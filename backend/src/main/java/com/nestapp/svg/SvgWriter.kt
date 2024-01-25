package com.nestapp.svg

import com.nestapp.DxfPartPlacement
import com.nestapp.nest.util.IOUtils
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Paths

class SvgWriter {

    private companion object {
        val COLORS = listOf("#7bafd1", "#fc8d8d", "#a6d854", "#ffd92f", "#e78ac3", "#66c2a5")
    }

    fun writeNestPathsToSvg(
        list: List<DxfPartPlacement>,
        fileName: String,
        width: Double,
        height: Double,
    ) {
        writeNestPathsToSvg(list, File(fileName), width, height)
    }

    fun writeNestPathsToSvg(
        list: List<DxfPartPlacement>,
        file: File,
        width: Double,
        height: Double,
    ) {
        println("Width $width, height $height")
        val rawSvg = buildString {
            for ((index, part) in list.withIndex()) {
                val ox = part.placement.translate.x
                val oy = part.placement.translate.y
                val rotate = part.placement.rotate
                appendLine("""<g transform="translate($ox, $oy) rotate($rotate)">""".trimIndent())
                appendLine("""<path d="""".trimIndent())

                part.nestPath.segments.forEachIndexed { index, segment ->
                    if (index == 0) {
                        append("M")
                    } else {
                        append("L")
                    }

                    val segment = part.nestPath[index]
                    append(segment.x)
                    append(" ")
                    append(segment.y)
                    append(" ")
                }

                //val color = if ((rotate == 0.0)) "7bafd1" else "fc8d8d"
                val color = COLORS[index % COLORS.size]
                appendLine("""Z" fill="$color" stroke="#010101" stroke-width="0.5" /> """)
                appendLine("</g>")
            }
        }

        saveStringsToSvgFile(rawSvg, file, width, height)
    }

    @Throws(Exception::class)
    fun saveStringsToSvgFile(string: String, file: File, width: Double, height: Double) {
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
