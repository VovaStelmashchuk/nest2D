package com.nestapp.nest

import java.awt.geom.Point2D

data class Placement(
    val translate: Point2D.Double,
    val rotate: Double,
)

val NotMovedPlacement = Placement(Point2D.Double(0.0, 0.0), 0.0)
