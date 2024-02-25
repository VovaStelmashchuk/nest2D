package com.nestapp.files

import com.nestapp.converts.makeListOfPoints
import com.nestapp.converts.makePath2d
import com.nestapp.files.svg.SvgWriter
import com.nestapp.nest.data.Placement
import com.nestapp.nest.data.Segment
import com.nestapp.project.parts.DxfPart
import com.nestapp.project.parts.PartsRepository
import java.awt.geom.Point2D
import java.io.File

class PreviewGenerator(
    private val partsRepository: PartsRepository,
) {

    companion object {
        private const val DXF_TO_SVG_TOLERANCE = 0.01
    }

    private val svgWriter = SvgWriter()

    fun createFilePreview(fileId: Int, svgFile: File) {
        val dxfParts = partsRepository.getPartByFileId(fileId)

        val points = dxfParts.flatMap { part ->
            getPartControlPoints(part)
        }

        val transactionX = points.minOf { it.x }
        val transactionY = points.minOf { it.y }
        val width = points.maxOf { it.x } - transactionX
        val height = points.maxOf { it.y } - transactionY

        val placement = dxfParts.map { part ->
            DxfPartPlacement(
                placement = Placement("", Segment(-transactionX, -transactionY), 0.0),
                part = part,
            )
        }

        svgWriter.writePlacement(placement, svgFile, width, height)
    }

    private fun getPartControlPoints(dxfPart: DxfPart): List<Point2D.Double> {
        val listOfPoints = makeListOfPoints(makePath2d(dxfPart.root), DXF_TO_SVG_TOLERANCE)

        val x = listOfPoints.minOf { it.x }
        val y = listOfPoints.minOf { it.y }
        val width = listOfPoints.maxOf { it.x }
        val height = listOfPoints.maxOf { it.y }

        return listOf(
            Point2D.Double(x, y),
            Point2D.Double(width, height),
        )
    }
}
