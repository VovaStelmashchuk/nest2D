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
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.awt.Rectangle
import java.io.File

fun Route.nestRestApi(projectsRepository: ProjectsRepository) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()
        nest(nestInput, projectsRepository)
        call.respond(HttpStatusCode.OK, "OK NESTED")
    }
}

private fun nest(
    nestInput: NestInput,
    projectsRepository: ProjectsRepository,
) {
    val dxfApi = DxfApi()

    val files = projectsRepository.getFiles(nestInput.projectId, nestInput.fileCounts.keys.toList())
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


    result.onSuccess { placement ->
        dxfApi.writeFile(placement, "mount/nested/test__11.dxf")

        println("Size on success ${nestInput.plateWidth} ${nestInput.plateHeight}")

        val svgWriter = SvgWriter()
        svgWriter.writeNestPathsToSvg(
            placement,
            "mount/nested/test__11.svg",
            nestInput.plateWidth.toDouble(),
            nestInput.plateHeight.toDouble()
        )
    }
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
