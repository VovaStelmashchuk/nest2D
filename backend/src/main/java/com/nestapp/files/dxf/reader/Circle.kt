package com.nestapp.files.dxf.reader

import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D

internal class Circle(type: String?) : Entity(type!!), AutoPop {
    private var circle: Ellipse2D.Double = Ellipse2D.Double()
    var cx: Double = 0.0
    var cy: Double = 0.0
    var radius: Double = 0.0

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> cx = value.toDouble()
            20 -> cy = value.toDouble()
            40 -> radius = value.toDouble()
        }
    }

    override fun close() {
        circle.setFrame(cx - radius, cy - radius, radius * 2, radius * 2)
    }

    override fun toPath2D(): Path2D.Double {
        val path = Path2D.Double()
        path.append(circle, false)
        return path
    }
}
