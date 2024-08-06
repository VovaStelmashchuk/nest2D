package com.nestapp.files.dxf

import com.nestapp.files.dxf.writter.parts.DXFEntity
import java.math.BigDecimal
import java.math.RoundingMode

fun setPrecision(value: Double): Double {
    return BigDecimal(value).setScale(10, RoundingMode.HALF_UP).toDouble()
}
