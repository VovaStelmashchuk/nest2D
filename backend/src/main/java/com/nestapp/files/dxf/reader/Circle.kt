package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.common.RealPoint
import com.nestapp.files.dxf.writter.parts.DXFCircle
import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.nest.Placement
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D

internal class Circle(type: String?) : Entity(type!!), AutoPop {

    private var cx: Double = 0.0
    private var cy: Double = 0.0
    private var radius: Double = 0.0

    private var path: Path2D.Double? = null

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> cx = value.toDouble()
            20 -> cy = value.toDouble()
            40 -> radius = value.toDouble()
        }
    }

    override fun close() {
        val circle = Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2)
        path = Path2D.Double()
        path!!.append(circle, false)
    }

    override fun toPath2D(): Path2D.Double {
        return path!!
    }

    override fun isClose(): Boolean {
        return true
    }

    override fun toWriterEntity(placement: Placement): DXFEntity {
        return DXFCircle(
            RealPoint(cx, cy).transform(placement),
            radius
        )
    }

    override fun translate(x: Double, y: Double): Entity {
        val newX = cx + x
        val newY = cy + y
        val originRadius = radius

        return Circle(type).apply {
            cx = newX
            cy = newY
            radius = originRadius
            close()
        }
    }
}
