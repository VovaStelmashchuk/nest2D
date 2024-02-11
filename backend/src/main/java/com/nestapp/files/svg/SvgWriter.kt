package com.nestapp.files.svg

import com.nestapp.files.dxf.DxfPart
import com.nestapp.files.dxf.DxfPartPlacement
import com.nestapp.nest.data.NestPath
import java.io.File
import java.io.FileWriter
import java.io.Writer

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    fun writeDxfPathsToSvg(
        dxfParts: List<DxfPart>,
        file: File,
    ) {
        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var maxY = Double.MIN_VALUE

        dxfParts.forEach { dxfPart ->
            if (minX > dxfPart.nestPath.minX) {
                minX = dxfPart.nestPath.minX
            }

            if (minY > dxfPart.nestPath.minY) {
                minY = dxfPart.nestPath.minY
            }

            if (maxX < dxfPart.nestPath.maxX) {
                maxX = dxfPart.nestPath.maxX
            }

            if (maxY < dxfPart.nestPath.maxY) {
                maxY = dxfPart.nestPath.maxY
            }
        }

        val translationX = -minX
        val translationY = -minY

        val width = maxX - minX
        val height = maxY - minY

        val rawSvg = buildString {
            for ((index, dxfPart) in dxfParts.withIndex()) {
                appendLine("""<g transform="translate($translationX, $translationY)">""".trimIndent())
                appendSvgPath(dxfPart.nestPath, index)
                dxfPart.inners.forEach { innerDxfPart ->
                    appendSvgPath(innerDxfPart.nestPath, index)
                }
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
                part.allNestedPath.forEach { nestPath ->
                    appendSvgPath(nestPath, index)
                }
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
