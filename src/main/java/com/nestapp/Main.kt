package com.nestapp

import com.jsevy.jdxf.DXFDocument
import com.jsevy.jdxf.parts.DXFLWPolyline
import com.jsevy.jdxf.parts.RealPoint
import com.nestapp.dxf.DXFReader
import com.nestapp.dxf.DXFReader.LwPolyline
import com.nestapp.dxf.DXFReader.LwPolyline.LSegment
import com.nestapp.nest.Nest
import com.nestapp.nest.config.Config
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import com.nestapp.nest.util.IOUtils
import com.nestapp.nest.util.SvgUtil
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Vector
import java.util.function.Consumer

internal object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val listOfDxfParts: MutableList<DxfPart> = ArrayList()
        listOfDxfParts.addAll(
            getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x1.dxf")
        )

        listOfDxfParts.addAll(
            getEntitiesFromFile("/Users/vovastelmashchuk/Desktop/dxf_app/Nest4J/input/1x4.dxf")
        )

        val list: MutableList<NestPath> = ArrayList()

        listOfDxfParts.forEach(Consumer { dxfPart: DxfPart ->
            list.add(dxfPart.nestPath)
        })

        val config = Config()
        config.USE_HOLE = false
        config.SPACING = 1.5

        val nest = Nest(binPolygon, list, config, 10)
        val appliedPlacement = nest.startNest()

        writeToDxf(appliedPlacement, listOfDxfParts, "test.dxf")

        val strings = SvgUtil.svgGenerator(list, appliedPlacement, 300.0, 300.0)
        IOUtils.saveSvgFile(strings, Config.OUTPUT_DIR + "test.svg")
    }

    @Throws(IOException::class)
    private fun writeToDxf(
        appliedPlacement: List<List<Placement>>,
        listOfDxfParts: List<DxfPart>,
        fileName: String
    ) {
        val document = DXFDocument()
        val firstPlacement = appliedPlacement[0]

        firstPlacement.forEach(Consumer { placement: Placement ->
            val dxfPart = getNestPathByBid(placement.bid, listOfDxfParts)!!
            val part = dxfPart.entity as LwPolyline

            val vertices = Vector<RealPoint>()

            println("id: " + placement.bid + " translate: " + placement.translate + "rotation " + placement.rotate)

            part.segments.forEach(Consumer { segment: LSegment ->
                vertices.add(
                    RealPoint(
                        segment.dx + (placement.translate.x * 0.039370078740157),
                        segment.dy + (placement.translate.y * 0.039370078740157),
                        0.0
                    )
                )
            })

            val translated = DXFLWPolyline(vertices.size, vertices, true)
            document.addEntity(translated)
        })

        val dxfText = document.toDXFString()
        val filePath = Config.OUTPUT_DIR + fileName
        val fileWriter = FileWriter(filePath)
        fileWriter.write(dxfText)
        fileWriter.flush()
        fileWriter.close()
    }

    private fun getNestPathByBid(bid: Int, list: List<DxfPart>): DxfPart? {
        for (nestPath in list) {
            if (nestPath.bid == bid) {
                return nestPath
            }
        }
        return null
    }

    private fun getEntitiesFromFile(fileName: String): List<DxfPart> {
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

            if (entity is LwPolyline) {
                entity.close()

                val nestPath = NestPath()
                entity.segments.forEach(Consumer { segment: LSegment ->
                    nestPath.add(segment.dx / dxfReader14.uScale, segment.dy / dxfReader14.uScale)
                })

                listOfListOfPoints.add(DxfPart(entity, nestPath))
            }
        }

        return listOfListOfPoints
    }

    private val binPolygon: NestPath
        get() {
            val binPolygon = NestPath()
            val width = 300.0
            val height = 300.0
            binPolygon.add(0.0, 0.0)
            binPolygon.add(0.0, height)
            binPolygon.add(width, height)
            binPolygon.add(width, 0.0)
            return binPolygon
        }
}
