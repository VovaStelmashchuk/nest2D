package com.nestapp.converts

import com.nestapp.nest.data.NestPath
import com.nestapp.project.parts.DataBaseDxfEntity
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

fun makePath2d(list: List<DataBaseDxfEntity>): Path2D.Double {
    return list
        .map { it.toPath2D() }
        .reduce { path, entityPath ->
            val interrator = entityPath.getPathIterator(null)
            val coords = DoubleArray(6)
            while (!interrator.isDone) {
                when (interrator.currentSegment(coords)) {
                    PathIterator.SEG_MOVETO -> path.moveTo(coords[0], coords[1])
                    PathIterator.SEG_LINETO -> path.lineTo(coords[0], coords[1])
                    PathIterator.SEG_QUADTO -> path.quadTo(coords[0], coords[1], coords[2], coords[3])
                    PathIterator.SEG_CUBICTO -> path.curveTo(
                        coords[0],
                        coords[1],
                        coords[2],
                        coords[3],
                        coords[4],
                        coords[5]
                    )

                    PathIterator.SEG_CLOSE -> path.closePath()
                }
                interrator.next()
            }

            return@reduce path
        }
}

fun makeNestPath(id: String, path: Path2D.Double, tolerance: Double): NestPath {
    val nestPath = NestPath(id)

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

fun makeListOfPoints(path: Path2D.Double, tolerance: Double): List<Point2D.Double> {
    val result = mutableListOf<Point2D.Double>()

    val at = AffineTransform()
    val iter = path.getPathIterator(at, tolerance)
    val coords = DoubleArray(6)
    while (!iter.isDone) {
        val type = iter.currentSegment(coords)

        when (type) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                result.add(Point2D.Double(coords[0], coords[1]))
            }

            PathIterator.SEG_QUADTO -> {
                result.add(Point2D.Double(coords[2], coords[3]))
            }

            PathIterator.SEG_CUBICTO -> {
                result.add(Point2D.Double(coords[4], coords[5]))
            }
        }

        iter.next()
    }
    return result
}


