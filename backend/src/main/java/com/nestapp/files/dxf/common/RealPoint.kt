package com.nestapp.files.dxf.common

import com.nestapp.nest.Placement
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
        val x1 = x * cos(placement.rotation) - y * sin(placement.rotation)
        val y1 = x * sin(placement.rotation) + y * cos(placement.rotation)

        val finalX = x1 + placement.x
        val finalY = y1 + placement.y

        return RealPoint(finalX, finalY)
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
