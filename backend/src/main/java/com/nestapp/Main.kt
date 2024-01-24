package com.nestapp

import com.nestapp.dxf.DxfApi
import com.nestapp.nest.config.Config
import com.nestapp.nest_api.NestApi
import com.nestapp.svg.SvgWriter
import java.awt.Rectangle
import java.io.File

internal object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Config.NFP_CACHE_PATH = "output/nfp.txt"
        val files = File("mount/uploads").list()!!
            .map {
                "mount/uploads/$it"
            }
        //val files = listOf("mount/uploads/1x1.dxf")

        files
            .filter { it.endsWith(".dxf") }
            .sortedBy { it }
            .takeLast(1)
            .forEachIndexed { index, fileName ->
                processFile(index + 3, fileName)
            }
    }

    private fun processFile(index: Int, fileName: String) {
        println(fileName)

        val dxfApi = DxfApi()
        val listOfDxfParts: MutableList<DxfPart> = ArrayList()
        listOfDxfParts.addAll(
            dxfApi.readFile(fileName)
        )

        val size = 350
        val nestApi = NestApi()
        val result: Result<List<DxfPartPlacement>> = nestApi.startNest(
            plate = Rectangle(size, size),
            dxfParts = listOfDxfParts,
        )

        result.onFailure {
            println(it)
        }

        println("size final $size")

        if (result.isSuccess) {
            val placement = result.getOrNull()!!.toList()
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
