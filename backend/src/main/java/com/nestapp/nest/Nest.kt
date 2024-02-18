package com.nestapp.nest

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import com.nestapp.nest.data.Result
import com.nestapp.nest.data.Segment
import com.nestapp.nest.nfp.NfpCacheRepository
import com.nestapp.nest.util.PlacementWorker
import io.ktor.util.logging.Logger

class Nest(
    private val logger: Logger,
    private val nfpCache: NfpCacheRepository,
) {

    fun startNest(
        binPolygon: NestPath,
        tree: List<NestPath>,
    ): List<Placement>? {
        nfpCache.addNestPaths(tree.plus(binPolygon))

        var best: Result? = null
        var bestArea = Double.MAX_VALUE

        val variants: List<List<NestPath?>> = generateNestListVariants(tree)

        logger.info("startNest(): variants.size() = ${variants.size}")

        variants.forEachIndexed { index, variant ->
            logger.info("startNest(): variant $index")
            val worker = PlacementWorker(nfpCache)
            val result = worker.placePaths(binPolygon, variant)

            if (result == null) {
                logger.info("startNest(): variant $index failed")
            } else {
                val placementMaxX = result.placements.maxOf { placement ->
                    val maxPathX = tree.find { path -> path.bid == placement.bid }!!.segments.maxOf { it.x }
                    return@maxOf placement.x + maxPathX
                }

                val placementMaxY = result.placements.maxOf { placement ->
                    val maxPathY = tree.find { path -> path.bid == placement.bid }!!.segments.maxOf { it.y }
                    return@maxOf placement.y + maxPathY
                }

                val area = placementMaxX * placementMaxY

                if (best == null || bestArea > area) {
                    bestArea = area
                    best = result
                }
            }
        }

        if (best == null) {
            return null
        } else {
            logger.info("startNest(): best fitness = $bestArea")
            return applyPlacement(best!!)
        }
    }

    private fun applyPlacement(best: Result): List<Placement> {
        return best.placements.map { pathPlacement ->
            Placement(
                pathPlacement.bid,
                Segment(pathPlacement.x, pathPlacement.y),
                pathPlacement.rotation.toDouble()
            )
        }
    }
}
