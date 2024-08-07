package com.nestapp.nest

import com.nestapp.TOLERANCE
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D

fun Path2D.Double.getPointsFromPath(): List<Point2D.Double> {
    val points = mutableListOf<Point2D.Double>()
    val iterator = this.getPathIterator(null, TOLERANCE)
    val coords = DoubleArray(6)

    while (!iterator.isDone) {
        when (iterator.currentSegment(coords)) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> points.add(Point2D.Double(coords[0], coords[1]))
        }
        iterator.next()
    }

    return points
}
