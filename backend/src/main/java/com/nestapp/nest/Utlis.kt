package com.nestapp.nest

import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import kotlin.math.abs

fun Path2D.Double.getPointsFromPath(tolerance: Double): List<Point2D.Double> {
    val points = mutableListOf<Point2D.Double>()
    val iterator = this.getPathIterator(null, tolerance)
    val coords = DoubleArray(6)

    while (!iterator.isDone) {
        when (iterator.currentSegment(coords)) {
            PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> points.add(Point2D.Double(coords[0], coords[1]))
        }
        iterator.next()
    }

    return points
}

fun area(points: List<Point2D.Double>): Double {
    if (points.size < 3) return 0.0

    var area = 0.0
    for (i in points.indices) {
        val j = (i + 1) % points.size
        area += points[i].x * points[j].y
        area -= points[j].x * points[i].y
    }
    return abs(area) / 2.0
}
