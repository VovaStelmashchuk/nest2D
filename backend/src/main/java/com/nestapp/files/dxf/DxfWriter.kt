package com.nestapp.files.dxf

import com.nestapp.files.dxf.writter.DXFDocument
import com.nestapp.nest.Placement
import com.nestapp.nest.jaguar.NestResult

class DxfWriter {

    fun buildDxfString(
        polygons: List<NestResult.NestedClosedPolygon>,
    ): String {
        val document = DXFDocument()
        document.setUnits(4)

        polygons.forEach { nestedPolygon ->
            val placement = Placement(
                rotation = nestedPolygon.rotation,
                x = nestedPolygon.x,
                y = nestedPolygon.y,
            )

            nestedPolygon.closePolygon.entities.forEach { entity ->
                document.addEntity(entity.toWriterEntity(placement))
            }
        }

        return document.toDXFString()
    }
}
