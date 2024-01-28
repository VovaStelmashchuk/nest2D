package com.nestapp.nest_api

import com.nestapp.DxfPart
import com.nestapp.DxfPartPlacement
import com.nestapp.nest.Nest
import com.nestapp.nest.config.Config
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.GeometryUtil
import java.awt.Rectangle

class NestApi {

    fun startNest(
        plate: Rectangle,
        dxfParts: List<DxfPart>,
    ): Result<List<DxfPartPlacement>> {
        if (dxfParts.isEmpty()) {
            return Result.failure(Throwable("Parts is empty"))
        }

        Config.BIN_WIDTH = plate.width.toDouble()
        Config.BIN_HEIGHT = plate.height.toDouble()
        Config.ASSUME_NO_INNER_PARTS = true

        val isAllPartFit = dxfParts.any { part ->
            val bound = GeometryUtil.getPolygonBounds(part.nestPath)
            bound.width > plate.width || bound.height > plate.height
        }

        if (isAllPartFit) {
            return Result.failure(Throwable("Has part cannot be fit"))
        }

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
            return Result.failure(CannotPlaceException())
        }

        val placements = appliedPlacement.first()
            .map { placement ->
                val dxfPart = dxfParts.find { dxfPart -> dxfPart.bid == placement.bid }!!
                DxfPartPlacement(
                    entities = dxfPart.entities,
                    nestPath = dxfPart.nestPath,
                    placement = placement,
                )
            }

        return Result.success(placements)
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

    class CannotPlaceException : Exception("Cannot place in one bin")
}
