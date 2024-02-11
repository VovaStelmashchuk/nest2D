package com.nestapp.files.dxf

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.dxf.writter.DXFDocument
import com.nestapp.nest.data.NestPath
import org.apache.batik.ext.awt.geom.Polygon2D
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
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

    fun readFile(file: File, tolerance: Double): List<DxfPart> {
        val dxfReader = DXFReader()
        try {
            dxfReader.parseFile(file)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return getEntities(dxfReader, tolerance)
    }

    private fun getEntities(dxfReader: DXFReader, tolerance: Double): List<DxfPart> {
        val connectedEntities: List<GroupedEntity> = EntityGrouper.groupEntities(dxfReader.entities, tolerance)
        val entitiesGroups: MutableMap<GroupedEntity, MutableList<GroupedEntity>> = mutableMapOf()

        for (parentIndex in connectedEntities.indices) {
            val parent: GroupedEntity = connectedEntities[parentIndex]
            for (childIndex in connectedEntities.indices) {
                if (parentIndex == childIndex) continue
                val child: GroupedEntity = connectedEntities[childIndex]

                if (isPathInsideAnother(parent.path, child.path, tolerance)) {
                    entitiesGroups.getOrPut(parent) { mutableListOf() }.add(child)
                }
            }
        }

        val singleGroups: List<DxfPart> =
            connectedEntities.minus(entitiesGroups.keys).minus(entitiesGroups.values.flatten().toSet())
                .map {
                    DxfPart(it.entities, toNestPath(it.path, tolerance))
                }

        val allGroupsToNest: List<DxfPart> = entitiesGroups.map { (parent, children) ->
            val childrenNestPaths = children.map { child -> DxfPart(child.entities, toNestPath(child.path, tolerance)) }
            return@map DxfPart(parent.entities, toNestPath(parent.path, tolerance), childrenNestPaths)
        } + singleGroups

        return allGroupsToNest
    }

    private fun isPathInsideAnother(outerPath: Path2D.Double, innerPath: Path2D.Double, tolerance: Double): Boolean {
        val parent = toNestPath(outerPath, tolerance).toPolygon2D()
        val child = toNestPath(innerPath, tolerance).toPolygon2D()

        return parent.contains(child)
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

    private fun toNestPath(path: Path2D, tolerance: Double): NestPath {
        val nestPath = NestPath()

        val at = AffineTransform()
        val iter = path.getPathIterator(at, tolerance)
        val coords = DoubleArray(6)
        while (!iter.isDone) {
            val type = iter.currentSegment(coords)

            when (type) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    nestPath.add(coords[0], coords[1])
                }

                PathIterator.SEG_QUADTO -> {
                    nestPath.add(coords[2], coords[3])
                }

                PathIterator.SEG_CUBICTO -> {
                    nestPath.add(coords[4], coords[5])
                }
            }
            iter.next()
        }

        return nestPath
    }

}

