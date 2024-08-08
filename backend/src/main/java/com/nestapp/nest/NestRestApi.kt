package com.nestapp.nest

import com.nestapp.Configuration
import com.nestapp.files.dxf.DxfWriter
import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.minio.MinioFileUpload
import com.nestapp.minio.ProjectRepository
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
    projectRepository: ProjectRepository,
    nestHistoryRepository: NestHistoryRepository,
    configuration: Configuration,
    minioFileUpload: MinioFileUpload,
) {
    post("/nest") {
        val nestInput = call.receive<NestInput>()

        val nestResultDatabase = nestHistoryRepository.createNestResult(nestInput.projectSlug)

        val closedPolygons = nestInput.fileCounts.filter { (_, count) ->
            count > 0
        }
            .flatMap { (file, count) ->
                val dxfReader = DXFReader()
                dxfReader.parseFile(projectRepository.getDxfFileAsStream(nestInput.projectSlug, file))
                val entities = dxfReader.entities
                polygonGenerator.getPolygons(entities, nestInput.tolerance).map {
                    JaguarNestInput.NestInputPolygons(
                        polygon = it,
                        count = count,
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
                )
            )

            val response: Any = when (nestedResult) {
                NestResult.NotFit -> {
                    NestedOutputError("NotFit")
                }

                is NestResult.Succes -> {
                    buildResultFiles(
                        nestedResult = nestedResult,
                        nestId = nestResultDatabase.id,
                        minioFileUpload = minioFileUpload,
                        configuration = configuration,
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
    nestedResult: NestResult.Succes,
    nestId: ObjectId,
    minioFileUpload: MinioFileUpload,
    configuration: Configuration
): NestedOutput {
    val svg = SvgWriter().buildNestedSvgString(nestedResult.polygons)

    val svgPath = "nested/${nestId.toHexString()}/preview.svg"

    minioFileUpload.uploadFileToMinioByteArray(
        bytes = svg.toByteArray(),
        contentType = "image/svg+xml",
        objectName = svgPath,
    )

    val dxfString = DxfWriter().buildDxfString(
        nestedResult.polygons,
    )

    val dxfPath = "nested/${nestId.toHexString()}/cad_file.dxf"

    minioFileUpload.uploadFileToMinioByteArray(
        bytes = dxfString.toByteArray(),
        contentType = "application/dxf",
        objectName = dxfPath,
    )

    return NestedOutput(
        id = nestId.toHexString(),
        svg = "${configuration.baseUrl}files/$svgPath",
        dxf = "${configuration.baseUrl}files/$dxfPath",
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
