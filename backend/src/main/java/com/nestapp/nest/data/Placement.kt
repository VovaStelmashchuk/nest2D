package com.nestapp.nest.data

class Placement(
    @JvmField var bid: Int,
    @JvmField var translate: Segment,
    @JvmField var rotate: Double
) {

    override fun toString(): String {
        return "Placement(bid=$bid, translate=$translate, rotate=$rotate)"
    }
}
