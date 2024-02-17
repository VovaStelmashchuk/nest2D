package com.nestapp.nest.data

class Bound(
    @JvmField var xmin: Double,
    @JvmField var ymin: Double,
    @JvmField var width: Double,
    @JvmField var height: Double
) {

    override fun toString(): String {
        return "xmin = $xmin , ymin = $ymin , width = $width, height = $height"
    }
}
