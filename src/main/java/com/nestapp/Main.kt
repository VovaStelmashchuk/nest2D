package com.nestapp

import com.nestapp.dxf.DxfApi
import com.nestapp.nest.config.Config
import com.nestapp.svg.SvgWriter
import java.awt.Rectangle
import java.io.File

internal object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Config.NFP_CACHE_PATH = "output/nfp.txt"
        val files = File("mount/uploads").list()!!
        //val files = listOf("3x4.dxf")

        files
            .filter { it.endsWith(".dxf") }
            .sortedBy { it }
            .take(2)
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
            size += 50
            println("Size $size")
            val nestApi = NestApi()
            result = nestApi.startNest(
                plate = Rectangle(size, size),
                dxfParts = listOfDxfParts,
            )

            result.onFailure {
                println(it)
            }
        }

        println("size final $size")

        if (result.isSuccess) {
            val placement = result.getOrNull()!!
            //mount/user_inputs
            val nameWithoutExt = File(fileName).nameWithoutExtension

            val folder = File("mount/user_inputs/$nameWithoutExt+$index")
            folder.mkdirs()

            dxfApi.writeFile(placement, folder.path + "/$nameWithoutExt.dxf")

            println("Size on success ${size.toDouble()}")

            val svgWriter = SvgWriter()
            svgWriter.writeNestPathsToSvg(
                placement,
                folder.path + "/$nameWithoutExt.svg",
                size.toDouble(),
                size.toDouble()
            )
        }
    }
}
