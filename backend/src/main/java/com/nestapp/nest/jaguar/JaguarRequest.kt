package com.nestapp.nest.jaguar

import com.nestapp.Configuration
import com.nestapp.nest.ClosePolygon
import com.nestapp.nest.polygonOffset
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

sealed class NestResult {

    data object NotFit : NestResult()

    data class Success(
        val polygons: List<NestedClosedPolygon>,
    ) : NestResult()

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
    val tolerance: Double,
    val spacing: Double,
) {
    data class NestInputPolygons(
        val polygon: ClosePolygon,
        val demand: Int,
    )
}

class JaguarRequest(
    private val client: HttpClient,
    private val configuration: Configuration,
) {
    suspend fun makeNestByJaguar(
        jaguarNestInput: JaguarNestInput,
    ): NestResult {
        val request = buildRequest(jaguarNestInput)

        return try {
            println("JaguarRequest request $jaguarNestInput")
            val response = client.post("${configuration.jaguarUrl}run/") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<Response>()

            val firstLayout = response.solution.layouts.firstOrNull()
            firstLayout?.let {
                NestResult.Success(
                    it.placedItems.map { placedItem ->
                        NestResult.NestedClosedPolygon(
                            closePolygon = jaguarNestInput.polygons[placedItem.index].polygon,
                            rotation = placedItem.transformation.rotation,
                            x = placedItem.transformation.translation[0],
                            y = placedItem.transformation.translation[1],
                        )
                    }
                )
            } ?: let {
                NestResult.NotFit
            }
        } catch (e: Exception) {
            System.err.println("JaguarRequest error: $e")
            throw e
        }
    }

    private fun buildRequest(
        jaguarNestInput: JaguarNestInput,
    ): Request {
        val binWidth = jaguarNestInput.width
        val binHeight = jaguarNestInput.height
        val request = Request(
            input = Request.Input(
                name = "input-1",
                items = jaguarNestInput.polygons.map { polygon ->
                    val originPoints = polygon.polygon.points
                    val offsetPoints = polygonOffset(originPoints, jaguarNestInput.spacing, jaguarNestInput.tolerance)

                    Request.Input.Item(
                        demand = polygon.demand,
                        allowedOrientations = listOf(0, 90, 180, 270),
                        shape = Request.Input.Item.Shape(
                            points = offsetPoints.map { point ->
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
                                    listOf(0.0, binHeight),
                                    listOf(binWidth, binHeight),
                                    listOf(binWidth, 0.0),
                                )
                            )
                        )
                    )
                )
            ),
            config = Request.Config(
                cdeConfig = Request.Config.CdeConfig(
                    itemSurrogateConfig = Request.Config.CdeConfig.ItemSurrogateConfig()
                ),
                polySimplTolerance = jaguarNestInput.tolerance / 10,
            )
        )
        return request
    }
}
