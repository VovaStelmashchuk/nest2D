package com.nestapp

import com.nestapp.nest.Nest
import com.nestapp.nest.config.Config
import com.nestapp.nest.data.NestPath
import java.awt.Rectangle

class NestApi {

    fun startNest(
        plate: Rectangle,
        dxfParts: List<DxfPart>,
    ): List<DxfPartPlacement> {
        val nestPathPlace = createNestPath(plate)

        val config = Config()
        config.USE_HOLE = false
        config.SPACING = 1.5

        val nestPaths = dxfParts
            .map { it.nestPath }
            .toMutableList()

        val nest = Nest(nestPathPlace, nestPaths, config, 10)
        val appliedPlacement = nest.startNest()

        if (appliedPlacement.size > 1) {
            throw Exception("Cannot place in one bin")
        }
        val placements = appliedPlacement.first()

        return placements
            .map { placement ->
                val dxfPart = dxfParts.find { dxfPart -> dxfPart.bid == placement.bid }!!
                DxfPartPlacement(
                    entity = dxfPart.entity,
                    nestPath = dxfPart.nestPath,
                    placement = placement,
                )
            }
    }

    private fun createNestPath(rect: Rectangle): NestPath {
        val binPolygon = NestPath()
        val width = rect.width
        val height = rect.height

        binPolygon.add(0.0, 0.0)
        binPolygon.add(0.0, height.toDouble())
        binPolygon.add(width.toDouble(), height.toDouble())
        binPolygon.add(width.toDouble(), 0.0)
        return binPolygon
    }
}
