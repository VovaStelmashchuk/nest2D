package com.nestapp.nest

import com.nestapp.TOLERANCE
import com.nestapp.files.dxf.reader.Entity
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import kotlin.math.sqrt

class PolygonGenerator {

    fun getPolygons(entities: List<Entity>): List<ClosePolygon> {
        val closedPolygons = mutableListOf<MutableClosePolygon>()

        val closedEntities = entities.filter { it.isClose() }
        val notClosedEntities = entities.filterNot { it.isClose() }

        closedEntities.forEach { entity ->
            closedPolygons.add(MutableClosePolygon(getPointsFromPath(entity.toPath2D()), mutableListOf(entity)))
        }

        val combinedClosedPolygons = combineNonClosedEntities(notClosedEntities)
        closedPolygons.addAll(combinedClosedPolygons)

        println("polygons: ${closedPolygons.size}")

        val mergedPolygons = mergePolygons(closedPolygons)

        println("merged polygons: ${mergedPolygons.size}")

        return mergedPolygons.map { mutableClosePolygon ->
            ClosePolygon(
                removeNearDuplicates(mutableClosePolygon.points),
                mutableClosePolygon.entities
            )
        }
    }

    private fun removeNearDuplicates(
        points: List<Point2D.Double>,
        tolerance: Double = TOLERANCE
    ): List<Point2D.Double> {
        if (points.isEmpty()) return points

        val distinctPoints = mutableListOf<Point2D.Double>()
        var lastPoint = points.first()
        distinctPoints.add(lastPoint)

        for (point in points.drop(1)) {
            if (lastPoint != point) {
                distinctPoints.add(point)
                lastPoint = point
            }
        }

        return distinctPoints
    }

    private fun mergePolygons(polygons: List<MutableClosePolygon>): List<MutableClosePolygon> {
        val mergedPolygons = mutableListOf<MutableClosePolygon>()
        val mergedIndices = mutableSetOf<Int>()

        for (i in polygons.indices) {
            if (i in mergedIndices) continue
            val parent = polygons[i]
            val parentPath = createPathFromPoints(parent.points)

            for (j in polygons.indices) {
                if (i == j || j in mergedIndices) continue
                val child = polygons[j]
                val childPath = createPathFromPoints(child.points)

                if (isPolygonInside(parentPath, childPath)) {
                    parent.entities.addAll(child.entities)
                    mergedIndices.add(j)
                }
            }
            mergedPolygons.add(parent)
        }

        return mergedPolygons
    }

    private fun createPathFromPoints(points: List<Point2D.Double>): Path2D.Double {
        val path = Path2D.Double()
        if (points.isNotEmpty()) {
            path.moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { path.lineTo(it.x, it.y) }
            path.closePath()
        }
        return path
    }

    private fun isPolygonInside(parentPath: Path2D.Double, childPath: Path2D.Double): Boolean {
        val childPoints = getPointsFromPath(childPath)
        return childPoints.all { parentPath.contains(it) }
    }

    private fun getPointsFromPath(path: Path2D.Double): List<Point2D.Double> {
        val points = mutableListOf<Point2D.Double>()
        val iterator = path.getPathIterator(null, TOLERANCE)
        val coords = DoubleArray(6)

        while (!iterator.isDone) {
            when (iterator.currentSegment(coords)) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> points.add(Point2D.Double(coords[0], coords[1]))
            }
            iterator.next()
        }

        return points
    }

    private fun combineNonClosedEntities(entities: List<Entity>): List<MutableClosePolygon> {
        val closedPolygons = mutableListOf<MutableClosePolygon>()
        val entityQueue = entities.toMutableList()

        val notCombinedEntity = mutableListOf<Entity>()

        while (entityQueue.isNotEmpty()) {
            val currentEntity = entityQueue.removeAt(0)
            val combinedPath = Path2D.Double()
            combinedPath.append(currentEntity.toPath2D(), false)
            val combinedEntities = mutableListOf(currentEntity)

            var closed = false

            while (!closed && entityQueue.isNotEmpty()) {
                var closestEntityIndex = -1
                var closestDistance = Double.POSITIVE_INFINITY

                var addToEnd = true
                var reverseEntity = true
                entityQueue.forEachIndexed { index, entity ->
                    val lastPoint = getPointsFromPath(combinedPath).last()
                    val firstPoint = getPointsFromPath(combinedPath).first()

                    val entityFirstPoint = getPointsFromPath(entity.toPath2D()).first()
                    val entityLastPoint = getPointsFromPath(entity.toPath2D()).last()

                    val distanceToEnd = pointDistance(lastPoint, entityFirstPoint)
                    val distanceToStart = pointDistance(firstPoint, entityLastPoint)
                    val distanceEndToEnd = pointDistance(lastPoint, entityLastPoint)
                    val distanceStartToStart = pointDistance(firstPoint, entityFirstPoint)

                    if (distanceToEnd < closestDistance) {
                        closestDistance = distanceToEnd
                        closestEntityIndex = index
                        addToEnd = true
                        reverseEntity = false
                    }

                    if (distanceToStart < closestDistance) {
                        closestDistance = distanceToStart
                        closestEntityIndex = index
                        addToEnd = false
                        reverseEntity = false
                    }

                    if (distanceEndToEnd < closestDistance) {
                        closestDistance = distanceEndToEnd
                        closestEntityIndex = index
                        addToEnd = true
                        reverseEntity = true
                    }

                    if (distanceStartToStart < closestDistance) {
                        closestDistance = distanceStartToStart
                        closestEntityIndex = index
                        addToEnd = false
                        reverseEntity = true
                    }
                }

                if (closestEntityIndex != -1 && closestDistance < TOLERANCE) {
                    val nextEntity = entityQueue.removeAt(closestEntityIndex)
                    val nextPath = Path2D.Double()
                    if (reverseEntity) {
                        val points = getPointsFromPath(nextEntity.toPath2D())
                        for (i in points.size - 1 downTo 0) {
                            if (i == points.size - 1) {
                                nextPath.moveTo(points[i].x, points[i].y)
                            } else {
                                nextPath.lineTo(points[i].x, points[i].y)
                            }
                        }
                    } else {
                        nextPath.append(nextEntity.toPath2D(), false)
                    }

                    if (addToEnd) {
                        combinedPath.append(nextPath, false)
                    } else {
                        val newPath = Path2D.Double()
                        newPath.append(nextPath, false)
                        newPath.append(combinedPath, false)
                        combinedPath.reset()
                        combinedPath.append(newPath, false)
                    }
                    combinedEntities.add(nextEntity)
                } else {
                    closed = true
                }
            }

            if (isPathClosed(combinedPath)) {
                closedPolygons.add(MutableClosePolygon(getPointsFromPath(combinedPath), combinedEntities))
            } else {
                notCombinedEntity.add(currentEntity)
            }
        }

        return closedPolygons
    }

    private fun pointDistance(a: Point2D.Double, b: Point2D.Double): Double {
        return sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
    }

    private fun isPathClosed(path: Path2D.Double): Boolean {
        val points = getPointsFromPath(path)
        return pointDistance(points.first(), points.last()) < TOLERANCE
    }
}

private data class MutableClosePolygon(
    val points: List<Point2D.Double>,
    val entities: MutableList<Entity>,
)

data class ClosePolygon(
    val points: List<Point2D.Double>,
    val entities: List<Entity>,
)
