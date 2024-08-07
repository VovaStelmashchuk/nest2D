package com.nestapp.nest

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.minio.ProjectRepository
import com.nestapp.nest.jaguar.JaguarNestInput
import com.nestapp.nest.jaguar.JaguarRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

fun Route.nestRestApi(
    jaguarRequest: JaguarRequest,
    polygonGenerator: PolygonGenerator,
    projectRepository: ProjectRepository,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        println("nestInput: $nestInput")

        val closedPolygons = nestInput.fileCounts.filter { (_, count) ->
            count > 0
        }
            .flatMap { (file, count) ->
                val dxfReader = DXFReader()
                dxfReader.parseFile(projectRepository.getDxfFileAsStream(nestInput.projectSlug, file))
                val entities = dxfReader.entities
                polygonGenerator.getPolygons(entities).map {
                    JaguarNestInput.NestInputPolygons(
                        polygon = it,
                        count = count,
                    )
                }
            }

        val nested = jaguarRequest.makeNestByJaguar(
            jaguarNestInput = JaguarNestInput(
                polygons = closedPolygons,
                width = nestInput.plateWidth,
                height = nestInput.plateHeight,
            )
        )

        val svg = SvgWriter().buildNestedSvgString(nested.polygons)

        try {
            // write to mount/test.svg
            File("/Users/vovastelmashchuk/Desktop/nest2d_online/backend/mount/test.svg").createNewFile()
            File("/Users/vovastelmashchuk/Desktop/nest2d_online/backend/mount/test.svg").writeText(svg)

            println("SVG written to /Users/vovastelmashchuk/Desktop/nest2d_online/backend/mount/test.svg")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        call.respond(HttpStatusCode.OK)
    }
}

@Serializable
data class NestInput(
    @SerialName("project_slug")
    val projectSlug: String,
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
