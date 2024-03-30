package com.nestapp.files.dxf.reader

import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

internal class Polyline(type: String?) : Entity(type!!) {

    private var path: Path2D.Double? = null
    var points: MutableList<Vertex> = ArrayList()
    private var firstX = 0.0
    private var firstY = 0.0
    private var lastX = 0.0
    private var lastY = 0.0
    private var firstPoint = true
    private var close = false

    override fun addParam(gCode: Int, value: String) {
        if (gCode == 70) {
            val flags = value.toInt()
            close = (flags and 1) != 0
        }
    }

    override fun addChild(child: Entity?) {
        if (child is Vertex) {
            points.add(child)
        }
    }

    override fun close() {
        path = Path2D.Double()
        var bulge = 0.0
        for (vertex in points) {
            if (firstPoint) {
                firstPoint = false
                path!!.moveTo(vertex.xx.also { lastX = it }.also { firstX = it }, vertex.yy.also { lastY = it }
                    .also { firstY = it })
            } else {
                if (bulge != 0.0) {
                    path!!.append(getArcBulge(lastX, lastY, vertex.xx, vertex.yy, bulge), true)
                    lastX = vertex.xx
                    lastY = vertex.yy
                } else {
                    path!!.lineTo(vertex.xx.also { lastX = it }, vertex.yy.also { lastY = it })
                }
            }
            bulge = vertex.bulge
        }
        if (close) {
            if (bulge != 0.0) {
                path!!.append(getArcBulge(lastX, lastY, firstX, firstY, bulge), true)
            } else {
                path!!.closePath()
            }
        }
    }

    override fun toPath2D(): Path2D.Double {
        return path!!
    }

    /**
     * See: http://darrenirvine.blogspot.com/2015/08/polylines-radius-bulge-turnaround.html
     *
     * @param sx    Starting x for Arc
     * @param sy    Starting y for Arc
     * @param ex    Ending x for Arc
     * @param ey    Ending y for Arc
     * @param bulge bulge factor (bulge > 0 = clockwise, else counterclockwise)
     * @return Arc2D.Double object
     */
    private fun getArcBulge(sx: Double, sy: Double, ex: Double, ey: Double, bulge: Double): Arc2D.Double {
        val p1 = Point2D.Double(sx, sy)
        val p2 = Point2D.Double(ex, ey)
        val mp = Point2D.Double((p2.x + p1.x) / 2, (p2.y + p1.y) / 2)
        val bp = Point2D.Double(mp.x - (p1.y - mp.y) * bulge, mp.y + (p1.x - mp.x) * bulge)
        val u = p1.distance(p2)
        val b = (2 * mp.distance(bp)) / u
        val radius = u * ((1 + b * b) / (4 * b))
        val dx = mp.x - bp.x
        val dy = mp.y - bp.y
        val mag = sqrt(dx * dx + dy * dy)
        val cp = Point2D.Double(bp.x + radius * (dx / mag), bp.y + radius * (dy / mag))
        val startAngle = 180 - Math.toDegrees(atan2(cp.y - p1.y, cp.x - p1.x))
        val opp = u / 2
        val extent = Math.toDegrees(asin(opp / radius)) * 2
        val extentAngle = if (bulge >= 0) -extent else extent
        val ul = Point2D.Double(cp.x - radius, cp.y - radius)
        return Arc2D.Double(ul.x, ul.y, radius * 2, radius * 2, startAngle, extentAngle, Arc2D.OPEN)
    }
}

internal class Vertex(type: String?) : Entity(type!!) {
    var xx: Double = 0.0
    var yy: Double = 0.0
    var bulge: Double = 0.0

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> xx = value.toDouble()
            20 -> yy = value.toDouble()
            42 -> bulge = value.toDouble()
        }
    }
}
