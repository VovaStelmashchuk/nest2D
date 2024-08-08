package com.nestapp.nest

import com.nestapp.files.dxf.reader.Entity
import java.awt.geom.Path2D
import java.awt.geom.Point2D

class PolygonGenerator {

    fun getMergedAndCombinedPolygons(rawEntities: List<Entity>, tolerance: Double): List<ClosePolygon> {
        val paths = rawEntities.map { it.toPath2D() }

        val minX = paths.minOfOrNull { it.bounds2D.minX } ?: 0.0
        val minY = paths.minOfOrNull { it.bounds2D.minY } ?: 0.0

        val entities = rawEntities.map { it.translate(-minX, -minY) }

        val closedPolygons = mutableListOf<MutableClosePolygon>()

        val closedEntities = entities.filter { it.isClose() }
        val notClosedEntities = entities.filterNot { it.isClose() }

        closedEntities.forEach { entity ->
            closedPolygons.add(
                MutableClosePolygon(
                    entity.toPath2D().getPointsFromPath(tolerance),
                    mutableListOf(entity)
                )
            )
        }

        val combinedClosedPolygons = combineNonClosedEntities(notClosedEntities, tolerance)
        closedPolygons.addAll(combinedClosedPolygons)

        val mergedPolygons =
            mergePolygons(closedPolygons.sortedByDescending { area(it.points) })

        return mergedPolygons.map { mutableClosePolygon ->
            ClosePolygon(
                removeNearDuplicates(mutableClosePolygon.points, tolerance),
                mutableClosePolygon.entities
            )
        }
    }

    fun convertEntitiesToPolygons(entities: List<Entity>, tolerance: Double): List<List<Point2D.Double>> {
        val closedPolygons = mutableListOf<MutableClosePolygon>()

        val closedEntities = entities.filter { it.isClose() }
        val notClosedEntities = entities.filterNot { it.isClose() }

        closedEntities.forEach { entity ->
            closedPolygons.add(
                MutableClosePolygon(
                    entity.toPath2D().getPointsFromPath(tolerance),
                    mutableListOf(entity)
                )
            )
        }

        val combinedClosedPolygons = combineNonClosedEntities(notClosedEntities, tolerance)
        closedPolygons.addAll(combinedClosedPolygons)

        return closedPolygons.map { mutableClosePolygon ->
            removeNearDuplicates(mutableClosePolygon.points, tolerance)
        }
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

    private fun isPolygonInside(parentPath: Path2D.Double, childPoints: List<Point2D.Double>): Boolean {
        return childPoints.all { parentPath.contains(it) }
    }

    private fun combineNonClosedEntities(entities: List<Entity>, tolerance: Double): List<MutableClosePolygon> {
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
                    val lastPoint = combinedPath.getPointsFromPath(tolerance).last()
                    val firstPoint = combinedPath.getPointsFromPath(tolerance).first()

                    val entityFirstPoint = entity.toPath2D().getPointsFromPath(tolerance).first()
                    val entityLastPoint = entity.toPath2D().getPointsFromPath(tolerance).last()

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

                if (closestEntityIndex != -1 && closestDistance < tolerance) {
                    val nextEntity = entityQueue.removeAt(closestEntityIndex)
                    val nextPath = Path2D.Double()
                    if (reverseEntity) {
                        val points = nextEntity.toPath2D().getPointsFromPath(tolerance)
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

            if (isPathClosed(combinedPath, tolerance)) {
                closedPolygons.add(MutableClosePolygon(combinedPath.getPointsFromPath(tolerance), combinedEntities))
            } else {
                notCombinedEntity.add(currentEntity)
            }
        }

        return closedPolygons
    }

    private fun isPathClosed(path: Path2D.Double, tolerance: Double): Boolean {
        val points = path.getPointsFromPath(tolerance)
        return pointDistance(points.first(), points.last()) < tolerance
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
