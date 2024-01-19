package com.nestapp

import com.nestapp.dxf.DxfApi
import com.nestapp.svg.SvgWritter
import java.awt.Rectangle

internal object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val dxfApi = DxfApi()

        val listOfDxfParts: MutableList<DxfPart> = ArrayList()
        listOfDxfParts.addAll(
            dxfApi.readFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/mount/uploads/1x1.dxf")
        )

        listOfDxfParts.addAll(
            dxfApi.readFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/mount/uploads/1x2.dxf")
        )

        val nestApi = NestApi()

        val result = nestApi.startNest(
            plate = Rectangle(300, 300),
            dxfParts = listOfDxfParts,
        )

        dxfApi.writeFile(result, "test_yes.dxf")

        val svgWriter = SvgWritter()
        svgWriter.writeNestPathsToSvg(
            result,
            "/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/output/test_2.svg"
        )
    }
}
