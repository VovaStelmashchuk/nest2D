package com.nestapp.test

import com.nestapp.DxfPartPlacement
import com.nestapp.dxf.DxfApi
import com.nestapp.nest_api.NestApi
import com.nestapp.svg.SvgWriter
import java.awt.Rectangle
import java.io.File

fun testTrigger() {
    val width = 100
    val height = 100

    val dxfApi = DxfApi()
    val file = File("mount/uploads/z_2_rect_10x20_5x7.dxf")

    val dxfParts = dxfApi.readFile(file)

    dxfParts.forEach {
        println(it)
    }

    val nestApi = NestApi()

    val result: Result<List<DxfPartPlacement>> = nestApi.startNest(
        plate = Rectangle(width, height),
        dxfParts = dxfParts,
    )

    result.onFailure {
        println("Error: $it")
    }

    result.onSuccess { placements ->
        println("Success: $placements")

        val dxfFile = File("test-data/test.dxf")
        val svgFile = File("test-data/test.svg")

        dxfApi.writeFile(placements, dxfFile)

        val svgWriter = SvgWriter()
        svgWriter.writeNestPathsToSvg(
            placements,
            svgFile,
            width.toDouble(),
            height.toDouble(),
        )
    }
}
