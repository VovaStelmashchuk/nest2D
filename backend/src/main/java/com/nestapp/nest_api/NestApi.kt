package com.nestapp.nest_api

import com.nestapp.nest.Nest
import com.nestapp.nest.data.Bound
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import com.nestapp.nest.util.CommonUtil
import com.nestapp.nest.util.GeometryUtil
import com.nestapp.nest.util.NewCommonUtils.copyNestPaths
import io.ktor.util.logging.Logger
import java.awt.geom.Rectangle2D
import kotlin.math.cos
import kotlin.math.sin

class NestApi(
    private val nest: Nest,
    private val logger: Logger,
) {

    fun nest(
        plate: Rectangle2D.Double,
        nestPaths: List<NestPath>,
        spacing: Double,
        boundSpacing: Double,
        rotationCount: Int,
    ): Result<List<Placement>> {
        val isAllPartFit = nestPaths.any { part ->
            !checkIfCanBePlaced(plate, part, rotationCount)
        }

        if (isAllPartFit) {
            return Result.failure(UserInputExecution.TheInputHasPartsThatCannotFitInBin())
        }

        val binPolygon = createPlateNestPath(plate, boundSpacing)

        //Apply offsets
        val tree: List<NestPath> = copyNestPaths(nestPaths)
        CommonUtil.offsetTree(tree, 0.5 * spacing)

        /*
         * Make sure it's counterclockwise (rotazione antioraria) TODO why?
         * Need for NFP algorithm, may be
         */
        for (element in tree) {
            val start = element[0]
            val end = element[element.size() - 1]
            if (GeometryUtil.almostEqual(start.x, end.x) && GeometryUtil.almostEqual(start.y, end.y)) {
                element.pop()
            }
            if (GeometryUtil.polygonArea(element) > 0) {
                element.reverse()
            }
        }

        val appliedPlacement: List<Placement> = nest.startNest(binPolygon, tree)
            ?: return Result.failure(CannotPlaceException())

        return Result.success(appliedPlacement)
    }

    private fun createPlateNestPath(plate: Rectangle2D.Double, boundSpacing: Double): NestPath {
        val binPolygon = NestPath(createBinNestPath(plate, boundSpacing))

        val binMinX = binPolygon.segments.minOfOrNull { it.x } ?: throw IllegalStateException("")
        val binMinY = binPolygon.segments.minOfOrNull { it.y } ?: throw IllegalStateException("")

        for (i in 0 until binPolygon.size()) {
            binPolygon[i].x -= binMinX
            binPolygon[i].y -= binMinY
        }

        if (GeometryUtil.polygonArea(binPolygon) > 0) {
            binPolygon.reverse()
        }
        return binPolygon
    }

    private fun checkIfCanBePlaced(plate: Rectangle2D.Double, nestPath: NestPath, rotationCount: Int): Boolean {
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

    private fun createBinNestPath(rect: Rectangle2D.Double, boundSpacing: Double): NestPath {
        var binPolygon = NestPath()
        val width = rect.width
        val height = rect.height

        binPolygon.add(0.0, 0.0)
        binPolygon.add(0.0, height)
        binPolygon.add(width, height)
        binPolygon.add(width, 0.0)

        if (boundSpacing > 0) {
            val offsetBin = CommonUtil.polygonOffset(binPolygon, -boundSpacing)
            if (offsetBin.size == 1) {
                binPolygon = offsetBin[0]
            }
        }
        binPolygon.bid = "${rect.width}x${rect.height}+$boundSpacing"
        return binPolygon
    }

    class CannotPlaceException : Exception("Cannot place in one bin")
}
