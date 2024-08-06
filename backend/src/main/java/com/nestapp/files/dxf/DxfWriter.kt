package com.nestapp.files.dxf

import com.nestapp.files.dxf.writter.DXFDocument
import com.nestapp.nest.ClosePolygon
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DxfWriter {


    @Throws(IOException::class)
    fun writeFile(
        polygons: List<ClosePolygon>,
        file: File,
    ) {
        val document = DXFDocument()
        document.setUnits(4)

        polygons.flatMap { it.entities }.forEach { entity ->
            document.addEntity(entity.toWriterEntity())
        }

        val dxfText = document.toDXFString()
        val fileWriter = FileWriter(file)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }
}
