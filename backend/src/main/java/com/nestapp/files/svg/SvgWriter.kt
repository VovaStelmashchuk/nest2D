package com.nestapp.files.svg

import com.nestapp.nest.ClosePolygon
import java.awt.geom.Point2D
import kotlin.math.cos
import kotlin.math.sin

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    data class SvgPolygon(
        val points: List<Point2D.Double>,
        val rotation: Double,
        val x: Double,
        val y: Double,
    )

    fun buildNestedSvgString(
        polygons: List<SvgPolygon>,
    ): String {
        var width = 0.0
        var height = 0.0

        val rawSvg = buildString {
            polygons.forEachIndexed { partIndex, polygon ->
                val transformPoints = polygon.points.map {
                    val x = it.x
                    val y = it.y
                    val x1 = x * cos(polygon.rotation) - y * sin(polygon.rotation)
                    val y1 = x * sin(polygon.rotation) + y * cos(polygon.rotation)

                    val finalX = x1 + polygon.x
                    val finalY = y1 + polygon.y

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
        appendLine("""Z" fill="#64$color" fill-opacity="0.5" stroke="$color" stroke-width="1" /> """)
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
