package com.nestapp.nest_api

import com.nestapp.dxf.DxfApi
import com.nestapp.projects.FileId
import com.nestapp.projects.ProjectId
import com.nestapp.projects.ProjectsRepository
import com.nestapp.svg.SvgWriter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.awt.Rectangle
import java.io.File

fun Route.nestRestApi(
    projectsRepository: ProjectsRepository,
    nestedRepository: NestedRepository,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        val id = nestedRepository.getNextId()
        val result = nest(id, nestInput, projectsRepository)
        nestedRepository.addNested(result)

        val nestedOutput = NestedOutput(
            id = id,
        )

        call.respond(HttpStatusCode.OK, nestedOutput)
    }

    get("/nested/{id}.svg") {
        val id = call.parameters["id"]?.toInt() ?: throw Exception("Nested id not found")
        val nested = nestedRepository.getNested(id) ?: throw Exception("Nested not found")

        println("nested.svgFile ${nested.svgFile}")
        println("nested.dxfFile ${nested.dxfFile}")

        call.respond(HttpStatusCode.MultiStatus)
    }
}

private fun nest(
    id: Int,
    nestInput: NestInput,
    projectsRepository: ProjectsRepository,
): Nested {
    val dxfApi = DxfApi()

    println("nestInput $nestInput")

    val fileIds = nestInput.fileCounts
        .filter { it.value > 0 }
        .keys
        .toList()

    val files = projectsRepository.getFiles(nestInput.projectId, fileIds)
        .map { (fileId, file) ->
            File("mount", "user_inputs/${fileId.value}/${file.dxfFile}")
        }

    if (files.any { !it.exists() }) {
        throw Exception("Some files not found")
    }

    val listOfDxfParts = files
        .flatMap {
            dxfApi.readFile(it)
        }

    val nestApi = NestApi()

    val result = nestApi.startNest(
        plate = Rectangle(nestInput.plateWidth, nestInput.plateHeight),
        dxfParts = listOfDxfParts,
    )

    result.onFailure {
        throw Exception(it)
    }

    val project = projectsRepository.getProject(nestInput.projectId) ?: throw Exception("Project not found")
    val fileName = project.files
        .filter { (key, _) -> fileIds.contains(key) }
        .map { (_, value) -> value.name }
        .joinToString(prefix = "${id}_", separator = "_and_", limit = 100) { it }

    val folder = File("mount/nested/$fileName")
    folder.mkdir()

    val dxfFile = File(folder, "$fileName.dxf")
    val svgFile = File(folder, "$fileName.svg")

    result.onSuccess { placement ->
        dxfApi.writeFile(placement, dxfFile)

        println("Size on success ${nestInput.plateWidth} ${nestInput.plateHeight}")

        val svgWriter = SvgWriter()
        svgWriter.writeNestPathsToSvg(
            placement,
            svgFile,
            nestInput.plateWidth.toDouble(),
            nestInput.plateHeight.toDouble()
        )
    }

    return Nested(
        id = id,
        dxfFile = dxfFile.path,
        svgFile = svgFile.path,
        projectId = nestInput.projectId,
        fileCounts = nestInput.fileCounts,
        plateWidth = nestInput.plateWidth,
        plateHeight = nestInput.plateHeight,
    )
}

@Serializable
data class NestInput(
    @SerialName("project_id")
    val projectId: ProjectId,
    @SerialName("file_counts")
    val fileCounts: Map<FileId, Int>,
    @SerialName("plate_width")
    val plateWidth: Int,
    @SerialName("plate_height")
    val plateHeight: Int,
)

@Serializable
data class NestedOutput(
    @SerialName("id")
    val id: Int,
)
