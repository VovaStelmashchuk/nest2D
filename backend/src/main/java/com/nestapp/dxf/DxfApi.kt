package com.nestapp.dxf

import com.jsevy.jdxf.DXFDocument
import com.jsevy.jdxf.parts.DXFLWPolyline
import com.jsevy.jdxf.parts.RealPoint
import com.nestapp.DxfPart
import com.nestapp.DxfPartPlacement
import com.nestapp.nest.data.NestPath
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class DxfApi {

    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        fileName: String
    ) {
        writeFile(dxfPartPlacements, File(fileName))
    }

    @Throws(IOException::class)
    fun writeFile(
        dxfPartPlacements: List<DxfPartPlacement>,
        file: File,
    ) {
        val document = DXFDocument()
        document.setUnits(4)

        dxfPartPlacements.forEach { dxfPartPlacment ->
            val part = dxfPartPlacment.entity as DXFReader.LwPolyline

            val vertices = Vector<RealPoint>()

            val angle: Double = dxfPartPlacment.placement.rotate * Math.PI / 180

            val translateX = dxfPartPlacment.placement.translate.x
            val translateY = dxfPartPlacment.placement.translate.y

            part.segments.forEach { segment: DXFReader.LwPolyline.LSegment ->
                val originX = segment.dx / 0.039370078740157
                val originY = segment.dy / 0.039370078740157

                val rotatedX = originX * cos(angle) - originY * sin(angle)
                val rotatedY = originY * cos(angle) + originX * sin(angle)

                val translatedX = rotatedX + translateX
                val translatedY = rotatedY + translateY

                vertices.add(
                    RealPoint(
                        translatedX,
                        translatedY,
                        0.0
                    )
                )
            }

            val translated = DXFLWPolyline(vertices.size, vertices, true)
            document.addEntity(translated)
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

    fun readFile(fileName: String): List<DxfPart> {
        val dxfReader = DXFReader()
        try {
            dxfReader.parseFile(File(fileName))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return getEntities(dxfReader)
    }

    private fun getEntities(dxfReader: DXFReader): List<DxfPart> {
        val listOfListOfPoints: MutableList<DxfPart> = ArrayList()
        for (entity in dxfReader.entities) {
            println("entity $entity")

            if (entity is DXFReader.LwPolyline) {
                entity.close()

                val nestPath = NestPath()
                entity.segments.forEach { segment: DXFReader.LwPolyline.LSegment ->
                    nestPath.add(segment.dx / 0.039370078740157, segment.dy / 0.039370078740157)
                }

                nestPath.setPossibleNumberRotations(4)

                listOfListOfPoints.add(DxfPart(entity, nestPath))
            }
        }

        return listOfListOfPoints
    }


}