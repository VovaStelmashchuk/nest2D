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
        val document = DXFDocument()

        dxfPartPlacements.forEach { dxfPartPlacment ->
            val part = dxfPartPlacment.entity as DXFReader.LwPolyline

            val vertices = Vector<RealPoint>()

            val angle: Double = dxfPartPlacment.placement.rotate * Math.PI / 180
            val translateX = dxfPartPlacment.placement.translate.x * 0.039370078740157
            val translateY = dxfPartPlacment.placement.translate.y * 0.039370078740157

            part.segments.forEach { segment: DXFReader.LwPolyline.LSegment ->
                val originX = segment.dx
                val originY = segment.dy

                val rotatedX = originX * cos(angle) - originY * sin(angle)
                val rotatedY = originX * sin(angle) + originY * cos(angle)

                val finalX = rotatedX + translateX
                val finalY = rotatedY + translateY

                vertices.add(
                    RealPoint(
                        finalX,
                        finalY,
                        0.0
                    )
                )
            }

            val translated = DXFLWPolyline(vertices.size, vertices, true)
            document.addEntity(translated)
        }

        val dxfText = document.toDXFString()
        val fileWriter = FileWriter(fileName)
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
                    nestPath.add(segment.dx / dxfReader.uScale, segment.dy / dxfReader.uScale)
                }

                nestPath.setPossibleNumberRotations(4)

                listOfListOfPoints.add(DxfPart(entity, nestPath))
            }
        }

        return listOfListOfPoints
    }


}
