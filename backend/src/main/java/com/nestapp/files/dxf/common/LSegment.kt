package com.nestapp.files.dxf.common

import com.nestapp.nest.Placement

data class LSegment(
    val dx: Double,
    val dy: Double,
    var bulge: Double = 0.0
) {
    fun transform(placement: Placement): LSegment {
        val point = RealPoint(dx, dy)
        val transformPoint = point.transform(placement)
        return LSegment(transformPoint.x, transformPoint.y, bulge)
    }
}
