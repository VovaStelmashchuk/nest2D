package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.common.LSegment
import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

class LwPolyline internal constructor(type: String) : Entity(type), AutoPop {
    var segments: MutableList<LSegment> = ArrayList()

    private var cSeg: LSegment? = null
    private var xCp = 0.0
    private var yCp = 0.0
    private var hasXcp = false
    private var hasYcp = false
    private var close = false

    private var path: Path2D.Double? = null

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

            70 -> {
                val flags = value.toInt()
                close = (flags and 0x01) != 0
            }

            42 -> {
                cSeg!!.bulge = value.toDouble()
            }

            90 -> {
                value.toInt()
            }
        }
        if (hasXcp && hasYcp) {
            hasYcp = false
            hasXcp = false
            segments.add(LSegment(xCp, yCp).also { cSeg = it })
        }
    }

    override fun toPath2D(): Path2D.Double {
        return path!!
    }

    override fun close() {
        path = Path2D.Double()
        var first = true
        var lastX = 0.0
        var lastY = 0.0
        var firstX = 0.0
        var firstY = 0.0
        var bulge = 0.0
        for (seg in segments) {
            if (bulge != 0.0) {
                path!!.append(
                    getArcBulge(lastX, lastY, seg.dx.also { lastX = it }, seg.dy.also { lastY = it }, bulge),
                    true
                )
            } else {
                if (first) {
                    path!!.moveTo(seg.dx.also { lastX = it }.also { firstX = it }, seg.dy.also { lastY = it }
                        .also { firstY = it })
                    first = false
                } else {
                    path!!.lineTo(seg.dx.also { lastX = it }, seg.dy.also { lastY = it })
                }
            }
            bulge = seg.bulge
        }
        if (close) {
            if (bulge != 0.0) {
                path!!.append(getArcBulge(lastX, lastY, firstX, firstY, bulge), true)
            } else {
                path!!.lineTo(firstX, firstY)
            }
        }
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


