package com.nestapp.nest_api

import com.nestapp.Configuration
import com.nestapp.files.dxf.DxfPartPlacement
import com.nestapp.files.dxf.DxfApi
import com.nestapp.projects.FileId
import com.nestapp.projects.ProjectId
import com.nestapp.projects.ProjectsRepository
import com.nestapp.files.svg.SvgWriter
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.awt.Rectangle
import java.io.File

fun Route.nestRestApi(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
    nestedRepository: NestedRepository,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        if (!nestInput.fileCounts.any { it.value > 0 }) {
            throw UserInputExecution.NotFileSelectedException()
        }

        val id = nestedRepository.getNextId()
        val result = nest(id, nestInput, projectsRepository, configuration.projectsFolder)
        nestedRepository.addNested(result)

        val nestedOutput = NestedOutput(id = id)

        call.respond(HttpStatusCode.OK, nestedOutput)
    }

    get("/nested/{id}") {
        val id = call.parameters["id"]?.toInt() ?: throw Exception("Nested id not found")
        val format = call.request.queryParameters["format"] ?: throw Exception("Format not found")

        val nested = nestedRepository.getNested(id) ?: throw Exception("Nested not found")

        val file = when (format) {
            "svg" -> {
                File(nested.svgFile)
            }

            "dxf" -> {
                File(nested.dxfFile)
            }

            else -> {
                throw Exception("Format not supported")
            }
        }

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                file.name,
            ).toString()
        )
        call.respondFile(file)
    }
}

private fun nest(
    id: Int,
    nestInput: NestInput,
    projectsRepository: ProjectsRepository,
    projectsFolder: File,
): Nested {
    val dxfApi = DxfApi()

    val fileIds = nestInput.fileCounts
        .filter { it.value > 0 }
        .keys
        .toList()

    val files = projectsRepository.getFiles(nestInput.projectId, fileIds)
        .map { (fileId, file) ->
            File(
                projectsFolder,
                "${nestInput.projectId.value}/${fileId.value}/${file.dxfFile}"
            ) to nestInput.fileCounts[fileId]!!
        }
        .toMap()

    if (files.any { !it.key.exists() }) {
        throw UserInputExecution.SomethingWrongWithUserInput("Some files not found")
    }

    val listOfDxfParts = files
        .flatMap { (file, count) ->
            val partsFromFile = dxfApi.readFile(file, nestInput.tolerance)

            val result = buildList {
                repeat(count) {
                    addAll(partsFromFile)
                }
            }

            return@flatMap result
        }

    val nestApi = NestApi()

    val result: Result<List<DxfPartPlacement>> = nestApi.startNest(
        plate = Rectangle(nestInput.plateWidth, nestInput.plateHeight),
        dxfParts = listOfDxfParts,
    )

    result.onFailure {
        throw when (it) {
            is NestApi.CannotPlaceException -> {
                UserInputExecution.CannotPlaceAllPartsIntoOneBin()
            }

            else -> it
        }
    }

    val project = projectsRepository.getProject(nestInput.projectId)
        ?: throw UserInputExecution.SomethingWrongWithUserInput("Project not found")

    val nestedResultFolder = project.files
        .filter { (key, _) -> fileIds.contains(key) }
        .map { (_, value) -> value.name }
        .joinToString(prefix = "${id}_${project.id.value}_", separator = "_and_", limit = 100) { it }

    val folder = File("mount/nested/$nestedResultFolder")
    folder.mkdir()

    val dxfFile = File(folder, "$nestedResultFolder.dxf")
    val svgFile = File(folder, "$nestedResultFolder.svg")

    result.onSuccess { placement ->
        dxfApi.writeFile(placement, dxfFile)

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
    @SerialName("tolerance")
    val tolerance: Double = 0.01,
)

@Serializable
data class NestedOutput(
    @SerialName("id")
    val id: Int,
)
