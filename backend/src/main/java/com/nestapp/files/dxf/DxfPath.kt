package com.nestapp.files.dxf

import java.awt.geom.Point2D

data class DxfPath(
    val segments: List<Point2D.Double>,
)
