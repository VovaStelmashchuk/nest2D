package com.nestapp.files.dxf

import com.nestapp.nest.data.Placement
import java.io.Serializable
import kotlin.math.cos
import kotlin.math.sin

open class RealPoint : Serializable {
    @JvmField
    val x: Double

    @JvmField
    val y: Double

    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    constructor(other: RealPoint) {
        this.x = other.x
        this.y = other.y
    }

    fun transform(placement: Placement): RealPoint {
        val angle: Double = placement.rotate * Math.PI / 180
        val translateX = placement.translate.x
        val translateY = placement.translate.y

        val rotatedX = x * cos(angle) - y * sin(angle)
        val rotatedY = y * cos(angle) + x * sin(angle)

        val translatedX = rotatedX + translateX
        val translatedY = rotatedY + translateY

        return RealPoint(translatedX, translatedY)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealPoint

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
