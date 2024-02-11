package com.nestapp.files.dxf

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.dxf.reader.Line
import com.nestapp.files.dxf.reader.LwPolyline
import com.nestapp.files.dxf.writter.DXFDocument
import com.nestapp.nest.data.NestPath
import org.apache.batik.ext.awt.geom.Polygon2D
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

        if (dxfReader.entities.size != lwPolylineList.size + lines.size) {
            throw RuntimeException("Unsupported entity type")
        }

        val dxfGroups: MutableMap<DxfPart, MutableList<DxfPart>> = mutableMapOf()

        for (parentIndex in dxfParts.indices) {
            val parent = dxfParts[parentIndex]
            for (childIndex in dxfParts.indices) {
                if (parentIndex == childIndex) continue
                val child = dxfParts[childIndex]
                if (parent.polygon.contains(child.polygon)) {
                    dxfGroups.getOrPut(parent) { mutableListOf() }.add(child)
                }
            }
        }

        val singleParts = dxfParts.minus(dxfGroups.keys).minus(dxfGroups.values.flatten().toSet())

        return dxfGroups.map { (parent, children) ->
            DxfPart(parent.entities, parent.nestPath, children)
        } + singleParts
    }

    private fun Polygon2D.contains(polygon2D: Polygon2D): Boolean {
        return (0 until polygon2D.npoints)
            .map {
                polygon2D.xpoints[it].toDouble() to polygon2D.ypoints[it].toDouble()
            }
            .all { (x, y) ->
                this.contains(x, y)
            }
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

