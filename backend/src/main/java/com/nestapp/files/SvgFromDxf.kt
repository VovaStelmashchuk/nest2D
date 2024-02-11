package com.nestapp.files

import com.nestapp.files.dxf.DxfApi
import com.nestapp.files.svg.SvgWriter
import java.io.File

class SvgFromDxf {

    private val dxfApi = DxfApi()
    private val svgWriter = SvgWriter()

    fun convertDxfToSvg(dxfFile: File, svgFile: File) {
        val dxf = dxfApi.readFile(dxfFile)

        svgWriter.writeJustNestPathsToSvg(
            dxf.map { it.nestPath },
            svgFile,
        )
    }
}
