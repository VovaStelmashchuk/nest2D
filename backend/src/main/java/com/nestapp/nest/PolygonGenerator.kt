package com.nestapp.nest

import com.nestapp.TOLERANCE
import com.nestapp.files.dxf.reader.Entity
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import kotlin.math.sqrt

class PolygonGenerator {

    fun getPolygons(entities: List<Entity>): List<ClosePolygon> {
        val closedPolygons = mutableListOf<MutableClosePolygon>()

        val closedEntities = entities.filter { it.isClose() }
        val notClosedEntities = entities.filterNot { it.isClose() }

        closedEntities.forEach { entity ->
            closedPolygons.add(MutableClosePolygon(entity.toPath2D().getPointsFromPath(), mutableListOf(entity)))
        }

        val combinedClosedPolygons = combineNonClosedEntities(notClosedEntities)
        closedPolygons.addAll(combinedClosedPolygons)

        val mergedPolygons =
            mergePolygons(closedPolygons.sortedByDescending { calculateArea(createPathFromPoints(it.points)) })

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
            if (pointDistance(lastPoint, point) > tolerance) {
                distinctPoints.add(point)
                lastPoint = point
            }
        }

        return distinctPoints
    }

    private fun mergePolygons(polygons: List<MutableClosePolygon>): List<MutableClosePolygon> {
        val mergedPolygons = mutableListOf<MutableClosePolygon>()
        val mergedIndices = mutableSetOf<Int>()

        for (parentIndex in polygons.indices) {
            if (parentIndex in mergedIndices) continue
            val parent = polygons[parentIndex]
            val parentPath = createPathFromPoints(parent.points)

            for (childIndex in polygons.indices) {
                if (parentIndex == childIndex || childIndex in mergedIndices) continue
                val child = polygons[childIndex]

                if (isPolygonInside(parentPath, child.points)) {
                    parent.entities.addAll(child.entities)
                    mergedIndices.add(childIndex)
                }
            }
            mergedPolygons.add(parent)
        }

        return mergedPolygons
    }

    private fun calculateArea(path: Path2D.Double): Double {
        val points = path.getPointsFromPath()
        if (points.size < 3) return 0.0  // A polygon must have at least 3 points

        var area = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            area += points[i].x * points[j].y
            area -= points[j].x * points[i].y
        }
        return Math.abs(area) / 2.0
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

    private fun isPolygonInside(parentPath: Path2D.Double, childPoints: List<Point2D.Double>): Boolean {
        return childPoints.all { parentPath.contains(it) }
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
                    val lastPoint = combinedPath.getPointsFromPath().last()
                    val firstPoint = combinedPath.getPointsFromPath().first()

                    val entityFirstPoint = entity.toPath2D().getPointsFromPath().first()
                    val entityLastPoint = entity.toPath2D().getPointsFromPath().last()

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
                        val points = nextEntity.toPath2D().getPointsFromPath()
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
                closedPolygons.add(MutableClosePolygon(combinedPath.getPointsFromPath(), combinedEntities))
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
        val points = path.getPointsFromPath()
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
