package com.nestapp.nest_api

import com.nestapp.files.dxf.DxfPart
import com.nestapp.files.dxf.DxfPartPlacement
import com.nestapp.nest.Nest
import com.nestapp.nest.config.Config
import com.nestapp.nest.data.Bound
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.GeometryUtil
import java.awt.Rectangle
import kotlin.math.cos
import kotlin.math.sin

class NestApi {

    fun startNest(
        plate: Rectangle,
        dxfParts: List<DxfPart>,
    ): Result<List<DxfPartPlacement>> {
        if (dxfParts.isEmpty()) {
            return Result.failure(Throwable("Parts is empty"))
        }

        val isAllPartFit = dxfParts.any { part ->
            !checkIfCanBePlaced(plate, part.nestPath, 4)
        }

        if (isAllPartFit) {
            return Result.failure(UserInputExecution.TheInputHasPartsThatCannotFitInBin())
        }

        val config = Config()
        config.SPACING = 1.5

        val nestPaths = dxfParts
            .map { it.nestPath }
            .toMutableList()

        val nest = Nest(
            config
        )
        val appliedPlacement = nest.startNest(createNestPath(plate), nestPaths)

        if (appliedPlacement.size > 1) {
            return Result.failure(CannotPlaceException())
        }

        val placements = appliedPlacement.first()
            .map { placement ->
                val dxfPart: DxfPart = dxfParts.find { dxfPart -> dxfPart.bid == placement.bid }!!
                DxfPartPlacement(
                    dxfPart = dxfPart,
                    placement = placement,
                )
            }

        return Result.success(placements)
    }

    private fun checkIfCanBePlaced(plate: Rectangle, nestPath: NestPath, rotationCount: Int): Boolean {
        if (rotationCount == 0) {
            val bound = GeometryUtil.getPolygonBounds(nestPath)
            if (plate.width < bound.width || plate.height < bound.height) {
                return false
            }
        } else {
            for (j in 0 until rotationCount) {
                val rotatedBound = rotatePolygon(nestPath, (360 / rotationCount) * j)
                if (plate.width < rotatedBound.width || plate.height < rotatedBound.height) {
                    return false
                }
            }
        }

        return true
    }

    private fun rotatePolygon(polygon: NestPath, angle: Int): Bound {
        if (angle == 0) {
            return GeometryUtil.getPolygonBounds(polygon)
        }
        val Fangle = angle * Math.PI / 180
        val rotated = NestPath()
        for (i in 0 until polygon.size()) {
            val x = polygon.get(i).x
            val y = polygon.get(i).y
            val x1 = x * cos(Fangle) - y * sin(Fangle)
            val y1 = x * sin(Fangle) + y * cos(Fangle)
            rotated.add(x1, y1)
        }
        return GeometryUtil.getPolygonBounds(polygon)
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
