package com.nestapp.test

import com.nestapp.files.dxf.DxfApi
import com.nestapp.files.svg.SvgWriter
import com.nestapp.nest_api.NestApi
import java.awt.Rectangle
import java.io.File

fun testTrigger() {
    val file = File("mount/projects/big_box_v1/long_side_1+0/long_side_1.dxf")

    val dxfApi = DxfApi()

    val dxfParts = dxfApi.readFile(file)

    val nestApi = NestApi()
    val result = nestApi.startNest(
        plate = Rectangle(0, 0, 1000, 1000),
        dxfParts = dxfParts + dxfParts + dxfParts,
    )

    val dxfFile = File("test-data", "1.dxf")
    val svgFile = File("test-data", "1.svg")

    result.onSuccess { placement ->
        dxfApi.writeFile(placement, dxfFile)

        val svgWriter = SvgWriter()
        svgWriter.writeNestPathsToSvg(
            placement,
            svgFile,
            1000.0,
            1000.0,
        )
    }
}

