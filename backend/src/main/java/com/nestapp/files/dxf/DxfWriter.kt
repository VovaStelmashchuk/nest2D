package com.nestapp.files.dxf

import com.nestapp.files.DxfPartPlacement
import com.nestapp.files.dxf.writter.DXFDocument
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DxfWriter {


    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        file: File,
    ) {
        val document = DXFDocument()
        document.setUnits(4)

        dxfPartPlacements.forEach { dxfPartPlacement ->
            val placement = dxfPartPlacement.placement
            dxfPartPlacement.part.inside.plus(dxfPartPlacement.part.root)
                .map {
                    it.toWritableEntity(placement)
                }
                .forEach {
                    document.addEntity(it)
                }
        }

        val dxfText = document.toDXFString()
        val fileWriter = FileWriter(file)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }

}
