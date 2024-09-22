package com.nestapp.nest

import com.nestapp.Configuration
import com.nestapp.files.dxf.DxfWriter
import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.s3.S3FileUpload
import com.nestapp.s3.S3ProjectRepository
import com.nestapp.mongo.NestHistoryRepository
import com.nestapp.nest.jaguar.JaguarNestInput
import com.nestapp.nest.jaguar.JaguarRequest
import com.nestapp.nest.jaguar.NestResult
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

fun Route.nestRestApi(
    jaguarRequest: JaguarRequest,
    polygonGenerator: PolygonGenerator,
    s3ProjectRepository: S3ProjectRepository,
    nestHistoryRepository: NestHistoryRepository,
    configuration: Configuration,
    s3FileUpload: S3FileUpload,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        val nestResultDatabase = nestHistoryRepository.createNestResult(nestInput.projectSlug)

        val closedPolygons = nestInput.fileCounts.filter { (_, count) ->
            count > 0
        }
            .flatMap { (file, count) ->
                val dxfReader = DXFReader()
                dxfReader.parseFile(s3ProjectRepository.getDxfFileAsStream(nestInput.projectSlug, file))
                val entities = dxfReader.entities
                polygonGenerator.getMergedAndCombinedPolygons(entities, nestInput.tolerance).map {
                    JaguarNestInput.NestInputPolygons(
                        polygon = it,
                        demand = count,
                    )
                }
            }

        try {
            val nestedResult = jaguarRequest.makeNestByJaguar(
                jaguarNestInput = JaguarNestInput(
                    polygons = closedPolygons,
                    width = nestInput.plateWidth,
                    height = nestInput.plateHeight,
                    tolerance = nestInput.tolerance,
                    spacing = nestInput.spacing,
                )
            )

            val response: Any = when (nestedResult) {
                NestResult.NotFit -> {
                    NestedOutputError("NotFit")
                }

                is NestResult.Success -> {
                    val result = buildResultFiles(
                        nestedResult = nestedResult,
                        nestId = nestResultDatabase.id,
                        s3FileUpload = s3FileUpload,
                    )

                    nestHistoryRepository.makeNestFinish(
                        id = nestResultDatabase.id,
                        svgPath = result.svg,
                        dxfPath = result.dxf,
                    )

                    NestedOutput(
                        id = nestResultDatabase.id.toHexString(),
                        svg = "${configuration.s3Config.publicUrlStart}/${result.svg}",
                        dxf = "${configuration.s3Config.publicUrlStart}/${result.dxf}",
                    )
                }
            }

            call.respond(HttpStatusCode.OK, response)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun buildResultFiles(
    nestedResult: NestResult.Success,
    nestId: ObjectId,
    s3FileUpload: S3FileUpload
): NestedOutput {
    val svgPolygons = nestedResult.polygons.flatMap { nestedPolygon ->
        PolygonGenerator().convertEntitiesToPolygons(nestedPolygon.closePolygon.entities, 0.1).map {
            SvgWriter.SvgPolygon(
                points = it,
                rotation = nestedPolygon.rotation,
                x = nestedPolygon.x,
                y = nestedPolygon.y,
            )
        }
    }

    val svg = SvgWriter().buildNestedSvgString(svgPolygons)

    val svgPath = "nested/${nestId.toHexString()}/preview.svg"

    s3FileUpload.uploadFileToS3ByteArray(
        bytes = svg.toByteArray(),
        contentType = "image/svg+xml",
        objectName = svgPath,
    )

    val dxfString = DxfWriter().buildDxfString(
        nestedResult.polygons,
    )

    val dxfPath = "nested/${nestId.toHexString()}/cad_file.dxf"

    s3FileUpload.uploadFileToS3ByteArray(
        bytes = dxfString.toByteArray(),
        contentType = "application/dxf",
        objectName = dxfPath,
    )

    return NestedOutput(
        id = nestId.toHexString(),
        svg = svgPath,
        dxf = dxfPath,
    )
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
    val id: String,
    @SerialName("svg")
    val svg: String,
    @SerialName("dxf")
    val dxf: String,
)

@Serializable
data class NestedOutputError(
    @SerialName("reason")
    val reason: String,
)
