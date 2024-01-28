package com.nestapp

import com.nestapp.dxf.reader.Entity
import com.nestapp.dxf.reader.LwPolyline
import com.nestapp.dxf.writter.parts.DXFEntity
import com.nestapp.dxf.writter.parts.DXFLWPolyline
import com.nestapp.dxf.writter.parts.RealPoint
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import java.util.Vector
import kotlin.math.cos
import kotlin.math.sin

data class DxfPart(
    val entities: List<Entity>,
    val nestPath: NestPath,
) {

    val bid: Int
        get() = nestPath.bid
}

data class DxfPartPlacement(
    val entities: List<Entity>,
    val nestPath: NestPath,
    val placement: Placement,
) {
    fun getDXFEntities(): List<DXFEntity> {
        return entities.map { entity ->
            when (entity) {
                is LwPolyline -> getDXFLWPolyline(entity)
                else -> throw RuntimeException("Not support entity")
            }
        }
    }

    private fun getDXFLWPolyline(lwPolyline: LwPolyline): DXFLWPolyline {
        val vertices = Vector<RealPoint>()

        val angle: Double = placement.rotate * Math.PI / 180
        val translateX = placement.translate.x
        val translateY = placement.translate.y

        lwPolyline.segments.forEach { segment: LwPolyline.LSegment ->
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
