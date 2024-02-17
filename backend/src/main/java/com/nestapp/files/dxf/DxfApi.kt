package com.nestapp.files.dxf

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.dxf.writter.DXFDocument
import org.apache.batik.ext.awt.geom.Polygon2D
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
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
                    DxfPart(it.entities, toDxfPath(it.path, tolerance))
                }

        val allGroupsToNest: List<DxfPart> = entitiesGroups.map { (parent, children) ->
            val childrenNestPaths = children
                .map { child ->
                    InnerDxfPart(child.entities, toDxfPath(child.path, tolerance))
                }
            return@map DxfPart(parent.entities, toDxfPath(parent.path, tolerance), childrenNestPaths)
        } + singleGroups

        return allGroupsToNest
    }

    private fun isPathInsideAnother(outerPath: Path2D.Double, innerPath: Path2D.Double, tolerance: Double): Boolean {
        val parentNestPath = toDxfPath(outerPath, tolerance)
        val childNestPath = toDxfPath(innerPath, tolerance)
        val parent = toPolygon2D(parentNestPath)
        val child = toPolygon2D(childNestPath)

        return parent.contains(child)
    }

    private fun toPolygon2D(nestPath: DxfPath): Polygon2D { ///TODO optimize
        val xp: MutableList<Float> = ArrayList()
        val yp: MutableList<Float> = ArrayList()
        for (s in nestPath.segments) {
            xp.add(s.getX().toFloat())
            yp.add(s.getY().toFloat())
        }

        val xparray = FloatArray(xp.size)
        val yparray = FloatArray(yp.size)
        var i = 0

        for (f in xp) {
            xparray[i++] = f
        }
        i = 0
        for (f in yp) {
            yparray[i++] = f
        }

        return Polygon2D(xparray, yparray, nestPath.segments.size)
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

    private fun toDxfPath(path: Path2D, tolerance: Double): DxfPath {
        val list = mutableListOf<Point2D.Double>()

        val at = AffineTransform()
        val iter = path.getPathIterator(at, tolerance)
        val coords = DoubleArray(6)
        while (!iter.isDone) {
            val type = iter.currentSegment(coords)

            when (type) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    list.add(Point2D.Double(coords[0], coords[1]))
                }

                PathIterator.SEG_QUADTO -> {
                    list.add(Point2D.Double(coords[2], coords[3]))
                }

                PathIterator.SEG_CUBICTO -> {
                    list.add(Point2D.Double(coords[4], coords[5]))
                }
            }
            iter.next()
        }

        return DxfPath(list)
    }
}
