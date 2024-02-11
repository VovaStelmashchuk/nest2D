package com.nestapp.files.dxf.reader

import java.awt.geom.Path2D


class Line internal constructor(type: String) : Entity(type), AutoPop {
    var xStart: Double = 0.0
    var yStart: Double = 0.0
    var xEnd: Double = 0.0
    var yEnd: Double = 0.0

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

    override fun toString(): String {
        return "Line{" +
            "xStart=" + xStart +
            ", yStart=" + yStart +
            ", xEnd=" + xEnd +
            ", yEnd=" + yEnd +
            '}'
    }
}
