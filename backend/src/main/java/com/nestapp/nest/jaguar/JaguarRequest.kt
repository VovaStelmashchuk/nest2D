package com.nestapp.nest.jaguar

import com.nestapp.nest.ClosePolygon
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

data class NestResult(
    val polygons: List<NestedClosedPolygon>,
) {
    data class NestedClosedPolygon(
        val closePolygon: ClosePolygon,
        val rotation: Double,
        val x: Double,
        val y: Double,
    )
}

data class JaguarNestInput(
    val polygons: List<NestInputPolygons>,
    val width: Double,
    val height: Double,
) {
    data class NestInputPolygons(
        val polygon: ClosePolygon,
        val count: Int,
    )
}

class JaguarRequest(
    private val client: HttpClient,
) {
    suspend fun makeNestByJaguar(
        jaguarNestInput: JaguarNestInput,
    ): NestResult {
        val request = buildRequest(jaguarNestInput)

        return try {
            val response = client.post("https://jagua.nest2d.online/run/") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<Response>()

            val nestedPolygons = response.solution.layouts.first().placedItems.map { placedItem ->
                NestResult.NestedClosedPolygon(
                    closePolygon = jaguarNestInput.polygons[placedItem.index].polygon,
                    rotation = placedItem.transformation.rotation,
                    x = placedItem.transformation.translation[0],
                    y = placedItem.transformation.translation[1],
                )
            }

            NestResult(
                polygons = nestedPolygons,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun buildRequest(
        jaguarNestInput: JaguarNestInput,
    ): Request {
        val width = jaguarNestInput.width
        val height = jaguarNestInput.height
        val request = Request(
            input = Request.Input(
                name = "input-1",
                items = jaguarNestInput.polygons.map { polygon ->
                    Request.Input.Item(
                        demand = polygon.count,
                        allowedOrientations = listOf(0, 90, 180, 270),
                        shape = Request.Input.Item.Shape(
                            points = polygon.polygon.points.map { point ->
                                listOf(point.x, point.y)
                            }
                        )
                    )
                },
                bins = listOf(
                    Request.Input.Bin(
                        cost = 1,
                        stock = 1,
                        shapes = Request.Input.Bin.Shape(
                            data = Request.Input.Bin.Shape.Data(
                                outer = listOf(
                                    listOf(0.0, 0.0),
                                    listOf(0.0, height),
                                    listOf(width, height),
                                    listOf(width, 0.0),
                                )
                            )
                        )
                    )
                )
            ),
            config = Request.Config(
                cdeConfig = Request.Config.CdeConfig(
                    itemSurrogateConfig = Request.Config.CdeConfig.ItemSurrogateConfig()
                )
            )
        )
        return request
    }

}
