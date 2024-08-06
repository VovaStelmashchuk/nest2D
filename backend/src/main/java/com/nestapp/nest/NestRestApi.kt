package com.nestapp.nest

import com.nestapp.project.ProjectSlug
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

fun Route.nestRestApi() {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        val projectFolder = File("mount/projects/${nestInput.projectSlug.value}")

        println(projectFolder.listFiles().map { it.name })

        /*val paths: List<NestPath> = nestInput.fileCounts.map { (fileName, count) ->
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
            .flatten()*/

        /*val tmpFile = File.createTempFile("input", ".svg")

        svgWriter.makeSvgWithAllPaths(tmpFile, paths)*/

        call.respond(HttpStatusCode.OK)
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
