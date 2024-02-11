package com.nestapp.files.dxf

import com.nestapp.files.dxf.reader.Entity
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import kotlin.math.abs

data class GroupedEntity(
    val entities: MutableList<Entity>,
    val path: Path2D.Double,
)

object EntityGrouper {

    private class PathEndPoints(
        var path: Path2D.Double,
        var startX: Double,
        var startY: Double,
        var endX: Double,
        var endY: Double
    )

    fun groupEntities(entities: List<Entity>): List<GroupedEntity> {
        val entityWithPathList: MutableList<EntityWithPath> = ArrayList()
        // Prepare entities with their path and endpoints
        for (entity in entities) {
            val path = entity.toPath2D()
            val endPoints: PathEndPoints = getPathEndPoints(path)
            entityWithPathList.add(EntityWithPath(entity, endPoints))
        }

        val groupedEntities: MutableList<GroupedEntity> = ArrayList()
        val grouped = BooleanArray(entities.size)

        for (i in entityWithPathList.indices) {
            if (grouped[i]) continue  // Skip already grouped entities

            var current = entityWithPathList[i]
            val group = GroupedEntity(
                mutableListOf(current.entity),
                Path2D.Double(current.endPoints.path) // Initialize the group's path
            )

            grouped[i] = true

            var didGroup: Boolean

            do {
                didGroup = false
                for (j in entityWithPathList.indices) {
                    if (i != j && !grouped[j]) {
                        if (canBeConnected(current.endPoints, entityWithPathList[j].endPoints)) {
                            // If can be connected, add to group and merge paths
                            group.entities.add(entityWithPathList[j].entity)
                            mergePaths(
                                group.path!!,
                                entityWithPathList[j].endPoints.path
                            ) // Merge the current group path with the new entity's path
                            current = entityWithPathList[j]
                            grouped[j] = true
                            didGroup = true
                        }
                    }
                }
            } while (didGroup)

            groupedEntities.add(group)
        }

        return groupedEntities
    }

    // Utility method to merge two Path2D.Double objects
    private fun mergePaths(path1: Path2D.Double, path2: Path2D.Double) {
        val pi = path2.getPathIterator(null)
        val coords = DoubleArray(6)
        while (!pi.isDone) {
            when (pi.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> path1.moveTo(coords[0], coords[1])
                PathIterator.SEG_LINETO -> path1.lineTo(coords[0], coords[1])
                PathIterator.SEG_QUADTO -> path1.quadTo(coords[0], coords[1], coords[2], coords[3])
                PathIterator.SEG_CUBICTO -> path1.curveTo(
                    coords[0],
                    coords[1],
                    coords[2],
                    coords[3],
                    coords[4],
                    coords[5]
                )

                PathIterator.SEG_CLOSE -> path1.closePath()
            }
            pi.next()
        }
    }

    private fun canBeConnected(p1: PathEndPoints?, p2: PathEndPoints?): Boolean {
        val tolerance = 0.0001
        if (p1 == null || p2 == null) return false

        val endToStartClose = abs(p1.endX - p2.startX) < tolerance && abs(p1.endY - p2.startY) < tolerance
        val startToEndClose = abs(p1.startX - p2.endX) < tolerance && abs(p1.startY - p2.endY) < tolerance

        return endToStartClose || startToEndClose
    }

    // Use the getPathEndPoints method from previous examples or implement one based on your logic
    private fun getPathEndPoints(path: Path2D.Double): PathEndPoints {
        val coords = DoubleArray(6)
        var startX = 0.0
        var startY = 0.0
        var endX = 0.0
        var endY = 0.0
        var isFirst = true

        val pi = path.getPathIterator(null)
        while (!pi.isDone) {
            val type = pi.currentSegment(coords)
            if (isFirst && type == PathIterator.SEG_MOVETO) {
                startX = coords[0]
                startY = coords[1]
                isFirst = false
            }
            if (type == PathIterator.SEG_LINETO || type == PathIterator.SEG_QUADTO || type == PathIterator.SEG_CUBICTO) {
                endX = coords[0]
                endY = coords[1]
            }
            pi.next()
        }

        return PathEndPoints(path, startX, startY, endX, endY)
    }


    private class EntityWithPath(val entity: Entity, val endPoints: PathEndPoints)
}
