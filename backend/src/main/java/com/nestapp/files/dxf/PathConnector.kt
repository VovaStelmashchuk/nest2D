package com.nestapp.files.dxf

import java.awt.geom.Path2D
import java.awt.geom.PathIterator

object PathConnector {
    // Helper method to get the start and end points of a Path2D.Double
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

    // Method to connect paths that can be connected
    fun connectPaths(paths: Array<Path2D.Double>): List<Path2D.Double> {
        val pathEndPointsList: MutableList<PathEndPoints> = ArrayList()
        for (path in paths) {
            pathEndPointsList.add(getPathEndPoints(path))
        }

        val connected = BooleanArray(paths.size) // Tracks which paths have been connected
        val connectedPaths: MutableList<Path2D.Double> = ArrayList()

        for (i in pathEndPointsList.indices) {
            if (connected[i]) continue  // Skip already connected paths


            val currentPath = pathEndPointsList[i].path
            var currentEndX = pathEndPointsList[i].endX
            var currentEndY = pathEndPointsList[i].endY
            var didConnect: Boolean

            do {
                didConnect = false
                for (j in pathEndPointsList.indices) {
                    if (i != j && !connected[j]) {
                        val nextStartX = pathEndPointsList[j].startX
                        val nextStartY = pathEndPointsList[j].startY

                        // Check if the current path's end matches the next path's start
                        if (currentEndX == nextStartX && currentEndY == nextStartY) {
                            // Append the next path to the current path
                            val pi = pathEndPointsList[j].path.getPathIterator(null)
                            val coords = DoubleArray(6)
                            while (!pi.isDone) {
                                val type = pi.currentSegment(coords)
                                when (type) {
                                    PathIterator.SEG_MOVETO ->                                         // Skip the first MOVETO to avoid disjoint segments
                                        if (pi.isDone) {
                                            currentPath.moveTo(coords[0], coords[1])
                                        }

                                    PathIterator.SEG_LINETO -> currentPath.lineTo(coords[0], coords[1])
                                    PathIterator.SEG_QUADTO -> currentPath.quadTo(
                                        coords[0],
                                        coords[1],
                                        coords[2],
                                        coords[3]
                                    )

                                    PathIterator.SEG_CUBICTO -> currentPath.curveTo(
                                        coords[0],
                                        coords[1],
                                        coords[2],
                                        coords[3],
                                        coords[4],
                                        coords[5]
                                    )

                                    PathIterator.SEG_CLOSE -> currentPath.closePath()
                                }
                                pi.next()
                            }

                            // Update the current path's end point
                            currentEndX = pathEndPointsList[j].endX
                            currentEndY = pathEndPointsList[j].endY
                            connected[j] = true // Mark the next path as connected
                            didConnect = true
                        }
                    }
                }
            } while (didConnect)

            connectedPaths.add(currentPath)
        }

        return connectedPaths
    }

    private class PathEndPoints(
        var path: Path2D.Double,
        var startX: Double,
        var startY: Double,
        var endX: Double,
        var endY: Double
    )
}
