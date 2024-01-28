package com.nestapp.dxf

import com.nestapp.DxfPart
import com.nestapp.DxfPartPlacement
import com.nestapp.dxf.reader.DXFReader
import com.nestapp.dxf.reader.Line
import com.nestapp.dxf.reader.LwPolyline
import com.nestapp.dxf.writter.DXFDocument
import com.nestapp.nest.data.NestPath
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DxfApi {

    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        file: File,
    ) {
        val document = DXFDocument()
        document.setUnits(4)

        dxfPartPlacements.forEach { dxfPartPlacement ->
            dxfPartPlacement.getDXFEntities().forEach { dxfEntity ->
                document.addEntity(dxfEntity)
            }
        }

        val dxfText = document.toDXFString()
        val fileWriter = FileWriter(file)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }

    fun readFile(file: File): List<DxfPart> {
        val dxfReader = DXFReader()
        try {
            dxfReader.parseFile(file)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return getEntities(dxfReader)
    }

    private fun getEntities(dxfReader: DXFReader): List<DxfPart> {
        val dxfParts = mutableListOf<DxfPart>()

        val lwPolylineList = dxfReader.entities.filterIsInstance<LwPolyline>()
        dxfParts.addAll(getLwParts(lwPolylineList))

        val lines = dxfReader.entities.filterIsInstance<Line>()
        dxfParts.addAll(getPartsFromLines(lines))

        return dxfParts.toList()
    }

    private fun getPartsFromLines(lines: List<Line>): List<DxfPart> {
        val createdShapes = mutableListOf<MutableList<Line>>()
        lines.forEach { line ->
            var added = false
            createdShapes.forEach { shape ->
                shape.find { it.xEnd == line.xStart && it.yEnd == line.yStart }?.let {
                    shape.add(line)
                    added = true
                }
            }

            if (!added) {
                createdShapes.add(mutableListOf(line))
            }
        }

        println("shapes: $createdShapes")

        return createdShapes.map { shape ->
            val nestPath = NestPath()
            shape.toList().forEach { line ->
                nestPath.add(line.xStart, line.yStart)
                nestPath.add(line.xEnd, line.yEnd)
            }
            return@map DxfPart(shape.toList(), nestPath)
        }
    }

    private fun getLwParts(
        lwPolylineList: List<LwPolyline>
    ): List<DxfPart> {
        return lwPolylineList.map { lwPolyline ->
            val nestPath = NestPath()
            lwPolyline.segments.forEach { segment: LwPolyline.LSegment ->
                nestPath.add(segment.dx, segment.dy)
            }
            nestPath.setPossibleNumberRotations(4)
            return@map DxfPart(listOf(lwPolyline), nestPath)
        }
    }
}

