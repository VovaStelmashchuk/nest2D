package com.nestapp.svg

import com.nestapp.DxfPartPlacement
import com.nestapp.nest.data.NestPath
import java.io.File
import java.io.FileWriter
import java.io.Writer

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    fun writeJustNestPathsToSvg(
        list: List<NestPath>,
        file: File,
    ) {
        var minX = 0.0
        var minY = 0.0
        var maxX = 0.0
        var maxY = 0.0
        list.forEach { nestPath ->
            nestPath.segments.forEach { segment ->
                if (segment.x < minX) {
                    minX = segment.x
                }
                if (segment.y < minY) {
                    minY = segment.y
                }
                if (segment.x > maxX) {
                    maxX = segment.x
                }
                if (segment.y > maxY) {
                    maxY = segment.y
                }
            }
        }

        val translationX = -minX
        val translationY = -minY

        val width = maxX - minX
        val height = maxY - minY

        val rawSvg = buildString {
            for ((index, nestPath) in list.withIndex()) {
                appendLine("""<g transform="translate($translationX, $translationY)">""".trimIndent())
                appendSvgPath(nestPath, index)
                appendLine("</g>")
            }
        }

        saveStringsToSvgFile(rawSvg, file, width, height)
    }

    fun writeNestPathsToSvg(
        list: List<DxfPartPlacement>,
        file: File,
        width: Double,
        height: Double,
    ) {
        val rawSvg = buildString {
            for ((index, part) in list.withIndex()) {
                val ox = part.placement.translate.x
                val oy = part.placement.translate.y
                val rotate = part.placement.rotate
                appendLine("""<g transform="translate($ox, $oy) rotate($rotate)">""".trimIndent())
                appendSvgPath(part.nestPath, index)
                appendLine("</g>")
            }
        }

        saveStringsToSvgFile(rawSvg, file, width, height)
    }

    private fun StringBuilder.appendSvgPath(nestPath: NestPath, index: Int) {
        appendLine("""<path d="""".trimIndent())

        nestPath.segments.forEachIndexed { segmentIndex, segment ->
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
