package com.nestapp.files.dxf.common

import com.nestapp.nest.data.Placement
import kotlin.math.cos
import kotlin.math.sin

data class LSegment(
    val dx: Double,
    val dy: Double,
    var bulge: Double = 0.0
) {
    fun transform(placement: Placement): LSegment {
        val angle: Double = placement.rotate * Math.PI / 180
        val translateX = placement.translate.x
        val translateY = placement.translate.y

        val rotatedX = dx * cos(angle) - dy * sin(angle)
        val rotatedY = dy * cos(angle) + dx * sin(angle)

        val translatedX = rotatedX + translateX
        val translatedY = rotatedY + translateY

        return LSegment(translatedX, translatedY, bulge)
    }

}
