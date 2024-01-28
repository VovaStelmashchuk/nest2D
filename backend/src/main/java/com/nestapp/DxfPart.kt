package com.nestapp

import com.nestapp.dxf.writter.parts.DXFLWPolyline
import com.nestapp.dxf.writter.parts.RealPoint
import com.nestapp.dxf.reader.Entity
import com.nestapp.dxf.reader.LwPolyline
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import java.util.Vector
import kotlin.math.cos
import kotlin.math.sin

data class DxfPart(
    val entity: Entity,
) {
    val nestPath: NestPath by lazy {
        val nestPath = NestPath()
        entity as LwPolyline
        entity.segments.forEach { segment: LwPolyline.LSegment ->
            nestPath.add(segment.dx, segment.dy)
        }

        nestPath.setPossibleNumberRotations(4)
        nestPath
    }

    val bid: Int
        get() = nestPath.bid
}

data class DxfPartPlacement(
    val entity: Entity,
    val nestPath: NestPath,
    val placement: Placement,
) {
    fun getDXFLWPolyline(): DXFLWPolyline {
        val part = entity as LwPolyline

        val vertices = Vector<RealPoint>()

        val angle: Double = placement.rotate * Math.PI / 180

        val translateX = placement.translate.x
        val translateY = placement.translate.y

        part.segments.forEach { segment: LwPolyline.LSegment ->
            val originX = segment.dx
            val originY = segment.dy

            val rotatedX = originX * cos(angle) - originY * sin(angle)
            val rotatedY = originY * cos(angle) + originX * sin(angle)

            val translatedX = rotatedX + translateX
            val translatedY = rotatedY + translateY

            vertices.add(
                RealPoint(
                    translatedX,
                    translatedY,
                )
            )
        }

        return DXFLWPolyline(vertices.size, vertices, true)
    }
}
