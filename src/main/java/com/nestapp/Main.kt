package com.nestapp

import com.nestapp.dxf.DxfApi
import com.nestapp.svg.SvgWriter
import java.awt.Rectangle
import java.io.File

internal object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val files = File("mount/uploads").list()!!
        //val files = listOf("1x3.dxf")

        files
            .filter { it.endsWith(".dxf") }
            .sortedBy { it }
            .forEachIndexed { index, fileName ->
                processFile(index, fileName)
            }
    }

    private fun processFile(index: Int, fileName: String) {
        println(fileName)

        val dxfApi = DxfApi()
        val listOfDxfParts: MutableList<DxfPart> = ArrayList()
        listOfDxfParts.addAll(
            dxfApi.readFile("mount/uploads/$fileName")
        )

        var size = 0
        var result: Result<List<DxfPartPlacement>>? = null

        while (result?.isSuccess != true) {
            println("Size $size")
            size += 50
            val nestApi = NestApi()
            result = nestApi.startNest(
                plate = Rectangle(size, size),
                dxfParts = listOfDxfParts,
            )

            result.onFailure {
                println(it)
            }
        }

        result.onSuccess { placement ->
            //mount/user_inputs
            val nameWithoutExt = File(fileName).nameWithoutExtension

            val folder = File("mount/user_inputs/$nameWithoutExt+$index")
            folder.mkdirs()

            dxfApi.writeFile(placement, folder.path + "/$nameWithoutExt.dxf")

            val svgWriter = SvgWriter()
            svgWriter.writeNestPathsToSvg(placement, folder.path + "/$nameWithoutExt.svg")
        }
    }
}
