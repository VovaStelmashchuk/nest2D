package com.nestapp.nest_api

import com.nestapp.converts.makeNestPath
import com.nestapp.converts.makePath2d
import com.nestapp.project.ProjectSlug
import com.nestapp.project.ProjectsRepository
import com.nestapp.project.parts.DataBaseDxfEntity
import com.nestapp.project.parts.PartsRepository
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.awt.geom.Rectangle2D
import java.io.File

fun Route.nestRestApi(
    projectsRepository: ProjectsRepository,
    partsRepository: PartsRepository,
    nestedRepository: NestedRepository,
    nestApi: NestApi,
    json: Json,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        if (!nestInput.fileCounts.any { it.value > 0 }) {
            throw UserInputExecution.NotFileSelectedException()
        }

        val project = projectsRepository.getProject(nestInput.projectSlug) ?: throw NotFoundException()

        val paths = nestInput.fileCounts.map { (fileName, count) ->
            val fileParts = partsRepository.getParts(project.slug, fileName)
                .map {
                    it.id.value to json.decodeFromString<List<DataBaseDxfEntity>>(it.root)
                }
                .map { (id, parts) ->
                    val path = makePath2d(parts)
                    val nestPath = makeNestPath("$id+${nestInput.spacing}", path, nestInput.tolerance)

                    nestPath
                }
            (0 until count).map { fileParts }.flatten()
        }
            .flatten()

        val result = nestApi.nest(
            plate = Rectangle2D.Double(0.0, 0.0, nestInput.plateWidth, nestInput.plateHeight),
            nestPaths = paths,
            spacing = nestInput.spacing,
            boundSpacing = nestInput.plateSpacing,
            rotationCount = 4,
        )

        result.onFailure {
            throw when (it) {
                is NestApi.CannotPlaceException -> {
                    UserInputExecution.CannotPlaceAllPartsIntoOneBin()
                }

                else -> it
            }
        }

        val placement = result.getOrNull() ?: throw Exception("Nest result is null")

        val nestedId = nestedRepository.saveNestPlacement(placement, nestInput)
        val nestedOutput = NestedOutput(id = nestedId)
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

@Serializable
data class NestInput(
    @SerialName("project_slug")
    val projectSlug: ProjectSlug,
    @SerialName("file_counts")
    val fileCounts: Map<String, Int>,
    @SerialName("plate_width")
    val plateWidth: Double,
    @SerialName("plate_height")
    val plateHeight: Double,
    @SerialName("tolerance")
    val tolerance: Double = 0.01,
    @SerialName("spacing")
    val spacing: Double = 1.5,
    @SerialName("place_spacing")
    val plateSpacing: Double = 0.0,
)

@Serializable
data class NestedOutput(
    @SerialName("id")
    val id: Int,
)
