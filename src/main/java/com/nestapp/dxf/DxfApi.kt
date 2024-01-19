package com.nestapp.dxf

import com.jsevy.jdxf.DXFDocument
import com.jsevy.jdxf.parts.DXFLWPolyline
import com.jsevy.jdxf.parts.RealPoint
import com.nestapp.DxfPart
import com.nestapp.DxfPartPlacement
import com.nestapp.nest.config.Config
import com.nestapp.nest.data.NestPath
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Vector

class DxfApi {

    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        fileName: String
    ) {
        val document = DXFDocument()

        dxfPartPlacements.forEach { dxfPartPlacment ->
            val part = dxfPartPlacment.entity as DXFReader.LwPolyline

            val vertices = Vector<RealPoint>()

            part.segments.forEach { segment: DXFReader.LwPolyline.LSegment ->
                vertices.add(
                    RealPoint(
                        segment.dx + (dxfPartPlacment.placement.translate.x * 0.039370078740157),
                        segment.dy + (dxfPartPlacment.placement.translate.y * 0.039370078740157),
                        0.0
                    )
                )
            }

            val translated = DXFLWPolyline(vertices.size, vertices, true)
            document.addEntity(translated)
        }

        val dxfText = document.toDXFString()
        val filePath = Config.OUTPUT_DIR + fileName
        val fileWriter = FileWriter(filePath)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }


    fun readFile(fileName: String): List<DxfPart> {
        val dxfReader = DXFReader()
        try {
            dxfReader.parseFile(File(fileName))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return getEntities(dxfReader)
    }

    private fun getEntities(dxfReader14: DXFReader): List<DxfPart> {
        val listOfListOfPoints: MutableList<DxfPart> = ArrayList()
        for (entity in dxfReader14.entities) {
            println(entity)

            if (entity is DXFReader.LwPolyline) {
                entity.close()

                val nestPath = NestPath()
                entity.segments.forEach { segment: DXFReader.LwPolyline.LSegment ->
                    nestPath.add(segment.dx / dxfReader14.uScale, segment.dy / dxfReader14.uScale)
                }

                listOfListOfPoints.add(DxfPart(entity, nestPath))
            }
        }

        return listOfListOfPoints
    }


}
