package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.common.RealPoint
import com.nestapp.files.dxf.writter.parts.DXFArc
import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.nest.Placement
import java.awt.geom.Arc2D
import java.awt.geom.Path2D

internal class Arc(type: String?) : Entity(type!!), AutoPop {

    private var arc: Arc2D.Double = Arc2D.Double(Arc2D.OPEN)
    private var cx = 0.0
    private var cy = 0.0
    private var startAngle = 0.0
    private var endAngle = 0.0
    private var radius = 0.0
    private lateinit var path: Path2D.Double

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> cx = value.toDouble()
            20 -> cy = value.toDouble()
            40 -> radius = value.toDouble()
            50 -> startAngle = value.toDouble()
            51 -> endAngle = value.toDouble()
        }
    }

    override fun close() {
        arc.setFrame(cx - radius, cy - radius, radius * 2, radius * 2)
        // Make angle negative so it runs clockwise when using Arc2D.Double
        arc.angleStart = -startAngle
        val extent = startAngle - (if (endAngle < startAngle) endAngle + 360 else endAngle)
        arc.angleExtent = extent

        path = Path2D.Double()
        path.append(arc, true)
    }

    override fun translate(x: Double, y: Double): Entity {
        val translatedArc = Arc(type)
        translatedArc.cx = this.cx + x
        translatedArc.cy = this.cy + y
        translatedArc.radius = this.radius
        translatedArc.startAngle = this.startAngle
        translatedArc.endAngle = this.endAngle
        translatedArc.close()
        return translatedArc
    }

    override fun toWriterEntity(placement: Placement): DXFEntity {
        val center = RealPoint(cx, cy)

        return DXFArc(
            center.transform(placement),
            radius,
            startAngle,
            endAngle,
            true,
        )
    }

    override fun isClose(): Boolean {
        return false
    }

    override fun toPath2D(): Path2D.Double {
        return path
    }
}
