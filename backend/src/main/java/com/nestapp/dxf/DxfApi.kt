package com.nestapp.dxf

import com.nestapp.DxfPart
import com.nestapp.DxfPartPlacement
import com.nestapp.dxf.reader.DXFReader
import com.nestapp.dxf.writter.DXFDocument
import java.io.File
import java.io.FileWriter
import java.io.IOException

class DxfApi {

    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        file: File,
    ) {
        val document = DXFDocument()
        document.setUnits(4)

        dxfPartPlacements.forEach { dxfPartPlacement ->
            document.addEntity(dxfPartPlacement.getDXFLWPolyline())
        }

        val dxfText = document.toDXFString()
        val fileWriter = FileWriter(file)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }

    fun readFile(file: File): List<DxfPart> {
        val dxfReader = DXFReader()
        try {
            dxfReader.parseFile(file)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return getEntities(dxfReader)
    }

    private fun getEntities(dxfReader: DXFReader): List<DxfPart> {
        val listOfListOfPoints: MutableList<DxfPart> = ArrayList()
        for (entity in dxfReader.entities) {
            println("entity $entity")
            listOfListOfPoints.add(DxfPart(entity))
        }

        return listOfListOfPoints
    }
}
