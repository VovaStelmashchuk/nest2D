package com.nestapp.nest

import com.nestapp.TOLERANCE
import com.nestapp.nest.config.Config
import de.lighti.clipper.Clipper
import de.lighti.clipper.ClipperOffset
import de.lighti.clipper.Path
import de.lighti.clipper.Paths
import de.lighti.clipper.Point
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import kotlin.math.abs
import kotlin.math.sqrt

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

fun createPathFromPoints(points: List<Point2D.Double>): Path2D.Double {
    val path = Path2D.Double()
    if (points.isNotEmpty()) {
        path.moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { path.lineTo(it.x, it.y) }
        path.closePath()
    }
    return path
}

fun polygonOffset(
    polygon: List<Point2D.Double>,
    offset: Double,
    tolerance: Double
): List<Point2D.Double> {
    val path = Path()
    for (s in polygon) {
        val cc = toLongPoint(s.getX(), s.getY())
        path.add(Point.LongPoint(cc.x, cc.y))
    }

    val miterLimit = 2
    val co = ClipperOffset(miterLimit.toDouble(), Config.CURVE_TOLERANCE * Config.CLIIPER_SCALE)
    co.addPath(path, Clipper.JoinType.ROUND, Clipper.EndType.CLOSED_POLYGON)

    val solution = Paths()
    co.execute(solution, offset * Config.CLIIPER_SCALE)

    return removeNearDuplicates(clipperToNestPath(solution[0]), tolerance)
}

fun removeNearDuplicates(
    points: List<Point2D.Double>,
    tolerance: Double,
): List<Point2D.Double> {
    if (points.isEmpty()) return points

    val distinctPoints = mutableListOf<Point2D.Double>()
    var lastPoint = points.first()
    distinctPoints.add(lastPoint)

    for (point in points.drop(1)) {
        if (pointDistance(lastPoint, point) > tolerance) {
            distinctPoints.add(point)
            lastPoint = point
        }
    }

    return distinctPoints
}

fun pointDistance(a: Point2D.Double, b: Point2D.Double): Double {
    return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
}

private fun toLongPoint(x: Double, y: Double): Point.LongPoint {
    return Point.LongPoint((x * Config.CLIIPER_SCALE).toLong(), (y * Config.CLIIPER_SCALE).toLong())
}

private fun toDoublePoint(x: Long, y: Long): Point2D.Double {
    return Point2D.Double((x.toDouble() / Config.CLIIPER_SCALE), (y.toDouble() / Config.CLIIPER_SCALE))
}

private fun clipperToNestPath(polygon: Path): List<Point2D.Double> {
    val normal: MutableList<Point2D.Double> = ArrayList()
    for (element in polygon) {
        val javaPoint = toDoublePoint(element.x, element.y)
        normal.add(Point2D.Double(javaPoint.getX(), javaPoint.getY()))
    }
    return normal
}
