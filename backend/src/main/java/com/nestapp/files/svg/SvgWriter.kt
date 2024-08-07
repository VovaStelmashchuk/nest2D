package com.nestapp.files.svg

import com.nestapp.nest.ClosePolygon
import com.nestapp.nest.jaguar.NestResult
import java.awt.geom.Point2D
import kotlin.math.cos
import kotlin.math.sin

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    fun buildNestedSvgString(
        polygons: List<NestResult.NestedClosedPolygon>,
    ): String {
        var width = 0.0
        var height = 0.0

        val rawSvg = buildString {
            polygons.forEachIndexed { partIndex, dxfPartPlacement ->
                val transformPoints = dxfPartPlacement.closePolygon.points.map {
                    val x = it.x
                    val y = it.y
                    val x1 = x * cos(dxfPartPlacement.rotation) - y * sin(dxfPartPlacement.rotation)
                    val y1 = x * sin(dxfPartPlacement.rotation) + y * cos(dxfPartPlacement.rotation)

                    val finalX = x1 + dxfPartPlacement.x
                    val finalY = y1 + dxfPartPlacement.y

                    if (finalY > height) {
                        height = finalY
                    }

                    if (finalX > width) {
                        width = finalX
                    }

                    Point2D.Double(finalX, finalY)
                }

                appendLine("<g>")

                appendSvgPath(transformPoints, partIndex)

                appendLine("</g>")
            }
        }

        return buildFinalSvg(rawSvg, width, height)
    }


    fun buildSvgString(
        polygons: List<ClosePolygon>,
    ): String {
        val minX = polygons.flatMap { it.points }.minOf { it.x }
        val minY = polygons.flatMap { it.points }.minOf { it.y }
        val maxX = polygons.flatMap { it.points }.maxOf { it.x }
        val maxY = polygons.flatMap { it.points }.maxOf { it.y }

        val width = maxX - minX
        val height = maxY - minY

        val rawSvg = buildString {
            polygons.forEachIndexed { partIndex, dxfPartPlacement ->
                // make translation x and y to make all coods > 0
                val translateX = -minX
                val translateY = -minY

                appendLine("""<g transform="translate($translateX, $translateY)">""".trimIndent())

                val points = dxfPartPlacement.points

                appendSvgPath(points, partIndex)

                appendLine("</g>")
            }
        }

        return buildFinalSvg(rawSvg, width, height)
    }

    private fun StringBuilder.appendSvgPath(points: List<Point2D.Double>, index: Int) {
        appendLine("""<path d="""".trimIndent())

        points.forEachIndexed { segmentIndex, segment ->
            if (segmentIndex == 0) {
                append("M")
            } else {
                append("L")
            }

            append(segment.x)
            append(" ")
            append(segment.y)
            append(" ")
        }

        val color = COLORS[index % COLORS.size]
        appendLine("""Z" fill="$color" stroke="#010101" stroke-width="0.5" /> """)
    }

    private fun buildFinalSvg(string: String, width: Double, height: Double): String {
        return buildString {
            append("<?xml version=\"1.0\" standalone=\"no\"?>")
            append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\"")
            append("  \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">")
            append("<svg  version=\"1.1\" viewBox=\"0 0 $width $height\"")
            append("  xmlns=\"http://www.w3.org/2000/svg\">")
            append(string)
            append("</svg>")
        }
    }
}
