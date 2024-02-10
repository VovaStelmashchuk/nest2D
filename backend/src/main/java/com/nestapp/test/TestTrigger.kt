package com.nestapp.test

import com.nestapp.dxf.DxfApi
import com.nestapp.svg.SvgWriter
import java.io.File

fun testTrigger() {
    val files = File("mount/uploads/laser_gridfinity_boxes_open_scad").list()?.toList()!!
        .sortedBy { it }

    files.forEachIndexed { index, it ->
        doFile(it, index)
    }

}

private fun doFile(file: String, index: Int) {
    val dxfAPi = DxfApi()
    val dxfFile = File("mount/uploads/laser_gridfinity_boxes_open_scad/${file}")
    val dxf = dxfAPi.readFile(dxfFile)

    dxf.map { it.nestPath }

    val svgWriter = SvgWriter()

    val name = file.removeSuffix(".dxf")

    val parentFolder = File("mount/projects/laser_gridfinity_boxes_open_scad/$name+$index")
    parentFolder.mkdir()

    svgWriter.writeJustNestPathsToSvg(
        dxf.map { it.nestPath },
        File(parentFolder, "$name.svg"),
    )

    dxfFile.copyTo(File(parentFolder, "$name.dxf"), true)
}
