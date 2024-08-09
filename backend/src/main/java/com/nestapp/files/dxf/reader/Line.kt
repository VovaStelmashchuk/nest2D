package com.nestapp.files.dxf.reader

import com.nestapp.files.dxf.common.RealPoint
import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.files.dxf.writter.parts.DXFLine
import com.nestapp.nest.Placement
import java.awt.geom.Path2D
import kotlin.properties.Delegates


class Line internal constructor(type: String) : Entity(type), AutoPop {
    private var xStart by Delegates.notNull<Double>()
    private var yStart by Delegates.notNull<Double>()
    private var xEnd by Delegates.notNull<Double>()
    private var yEnd by Delegates.notNull<Double>()

    private var line: Path2D.Double? = null

    override fun toPath2D(): Path2D.Double {
        return line!!
    }

    override fun addParam(gCode: Int, value: String) {
        when (gCode) {
            10 -> xStart = value.toDouble()
            20 -> yStart = value.toDouble()
            11 -> xEnd = value.toDouble()
            21 -> yEnd = value.toDouble()
        }
    }

    override fun close() {
        line = Path2D.Double().apply {
            moveTo(xStart, yStart)
            lineTo(xEnd, yEnd)
        }
    }

    override fun isClose(): Boolean {
        return xStart == xEnd && yStart == yEnd
    }

    override fun toWriterEntity(placement: Placement): DXFEntity {
        val start = RealPoint(xStart, yStart)
        val end = RealPoint(xEnd, yEnd)

        return DXFLine(start.transform(placement), end.transform(placement))
    }
}
