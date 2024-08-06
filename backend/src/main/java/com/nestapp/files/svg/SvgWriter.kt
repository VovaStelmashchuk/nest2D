package com.nestapp.files.svg

import com.nestapp.nest.ClosePolygon
import java.awt.geom.Point2D
import java.io.File
import java.io.FileWriter
import java.io.Writer

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    fun writePlacement(
        polygons: List<ClosePolygon>,
        file: File,
    ) {
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

                /*val translateX = dxfPartPlacement.placement.translate.x
                val translateY = dxfPartPlacement.placement.translate.y*/

                val rotation = 0.0//dxfPartPlacement.placement.rotate
                appendLine("""<g transform="translate($translateX, $translateY) rotate($rotation)">""".trimIndent())

                val points = dxfPartPlacement.points

                appendSvgPath(points, partIndex)

                appendLine("</g>")
            }
        }

        saveStringsToSvgFile(rawSvg, file, width, height)
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

    @Throws(Exception::class)
    private fun saveStringsToSvgFile(string: String, file: File, width: Double, height: Double) {
        file.createNewFile()

        val writer: Writer = FileWriter(file, false)
        writer.write(
            "<?xml version=\"1.0\" standalone=\"no\"?>\n" +
                "\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \n" +
                "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                " \n" +
                "<svg  version=\"1.1\" viewBox=\"0 0 " + width + " " + height + "\" \n" +
                "xmlns=\"http://www.w3.org/2000/svg\">\n"
        )
        writer.write(string)
        writer.write("</svg>")
        writer.close()
    }
}
