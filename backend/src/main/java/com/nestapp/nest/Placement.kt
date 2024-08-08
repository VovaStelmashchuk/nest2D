package com.nestapp.nest

data class Placement(
    val rotation: Double,
    val x: Double,
    val y: Double,
)

val NotMovedPlacement = Placement(0.0, 0.0, 0.0)
