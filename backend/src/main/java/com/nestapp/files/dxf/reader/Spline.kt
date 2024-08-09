package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.common.RealPoint
import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.files.dxf.writter.parts.DXFSpline
import com.nestapp.nest.Placement
import java.awt.geom.Path2D
import java.awt.geom.Point2D

internal class Spline(type: String?) : Entity(type!!), AutoPop {
    private var path: Path2D.Double = Path2D.Double()
    private var cPoints: MutableList<Point2D.Double> = ArrayList()
    private var xCp = 0.0
    private var yCp = 0.0
    private var hasXcp = false
    private var hasYcp = false
    private var closed = false
    private var numCPs = 0
    private var degree = 0
    private var numKnots = 0
    private val knots: MutableList<Double> = mutableListOf()

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> {
                xCp = value.toDouble()
                hasXcp = true
            }

            20 -> {
                yCp = value.toDouble()
                hasYcp = true
            }

            40 -> {
                // Handle knots (if needed)
                knots.add(value.toDouble())
            }

            /*70 -> {
                val flags = value.toInt()
                closed = (flags and 0x01) != 0
                // Additional flag handling if needed (e.g., periodic splines)
                periodic = (flags and 0x02) != 0
            }*/

            71 -> degree = value.toInt()

            72 -> numKnots = value.toInt()

            73 -> numCPs = value.toInt()

            /*74 -> {
                // Handle if spline is periodic
                periodic = value.toInt() == 1
            }*/
        }

        // Check if control point is fully defined
        if (hasXcp && hasYcp) {
            cPoints.add(Point2D.Double(xCp, yCp))
            hasXcp = false
            hasYcp = false

            // Handle when enough control points are gathered
            if (cPoints.size == numCPs) {
                processControlPoints()
            }
        }
    }

    // Helper function to process control points based on degree
    private fun processControlPoints() {
        if (degree == 3) {
            val points = cPoints.toTypedArray<Point2D.Double>()
            path.moveTo(points[0].x, points[0].y)
            var ii = 1
            while (ii < points.size) {
                path.curveTo(
                    points[ii].x, points[ii].y,
                    points[ii + 1].x, points[ii + 1].y,
                    points[ii + 2].x, points[ii + 2].y
                )
                ii += 3
            }
        } else if (degree == 2) {
            val points = cPoints.toTypedArray<Point2D.Double>()
            path.moveTo(points[0].x, points[0].y)
            var ii = 1
            while (ii < points.size) {
                path.quadTo(
                    points[ii].x, points[ii].y,
                    points[ii + 1].x, points[ii + 1].y
                )
                ii += 2
            }
        }
    }

    override fun toWriterEntity(placement: Placement): DXFEntity {
        // Convert the control points from the Spline class to the format needed for DXFSpline
        val controlPoints = mutableListOf<Double>()
        for (point in cPoints) {
            val realPoint = RealPoint(point.x, point.y)
            val resultPoint = realPoint.transform(placement)
            controlPoints.add(resultPoint.x)
            controlPoints.add(resultPoint.y)
        }

        // Ensure the knots list is correctly sized based on the degree and number of control points
        val knotsArray = knots.toDoubleArray()

        // Create the DXFSpline object
        val dxfSpline = DXFSpline(degree, controlPoints.toDoubleArray(), knotsArray)

        return dxfSpline
    }


    override fun isClose(): Boolean {
        return closed
    }

    override fun toPath2D(): Path2D.Double {
        return path
    }
}
