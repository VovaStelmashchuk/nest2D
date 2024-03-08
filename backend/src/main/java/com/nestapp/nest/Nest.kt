package com.nestapp.nest

import com.nestapp.Configuration
import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.PathPlacement
import com.nestapp.nest.data.Placement
import com.nestapp.nest.data.Segment
import com.nestapp.nest.nfp.NfpCacheReader
import com.nestapp.nest.nfp.NfpCacheRepository
import com.nestapp.nest.util.PlacementWorker
import io.ktor.util.logging.Logger
import java.util.concurrent.TimeUnit

class Nest(
    private val logger: Logger,
    private val nfpCache: NfpCacheRepository,
    private val nfpCacheReaderGetter: () -> NfpCacheReader,
    private val configuration: Configuration,
) {

    fun startNest(
        binPolygon: NestPath,
        tree: List<NestPath>,
    ): List<Placement>? {
        nfpCache.addNestPaths(tree.plus(binPolygon))

        var best: NestResult? = null
        val variants: List<List<NestPath>> = generateNestListVariants(tree)

        logger.info("startNest(): variants.size() = ${variants.size}")

        val maxTimeForNestProcess = TimeUnit.SECONDS.toMillis(configuration.maxNestTimeInSeconds)
        val currentTime = System.currentTimeMillis()

        val nfpCacheReader: NfpCacheReader = nfpCacheReaderGetter()

        var compliteVariantCount = 0

        for (index in variants.indices) {
            logger.info("startNest(): variant $index")
            val variant = variants[index]

            val newResult = tryPlacement(binPolygon, variant, tree, nfpCacheReader)

            if (newResult != null) {
                if (best == null || best.fitness > newResult.fitness) {
                    best = newResult
                }
            }

            if (System.currentTimeMillis() - currentTime > maxTimeForNestProcess) {
                logger.info("startNest(): time limit reached")
                break
            }
            compliteVariantCount++
        }

        if (best == null) {
            return null
        } else {
            logger.info("startNest(): best fitness = ${best.fitness}, complete variant count = $compliteVariantCount")
            return applyPlacement(best.placement)
        }
    }

    private fun tryPlacement(
        binPolygon: NestPath,
        variant: List<NestPath>,
        tree: List<NestPath>,
        nfpCacheReader: NfpCacheReader,
    ): NestResult? {
        val worker = PlacementWorker(nfpCache, nfpCacheReader)
        val result = worker.placePaths(binPolygon, variant)

        if (result == null) {
            return null
        } else {
            val placementMaxX = result.maxOf { placement ->
                val maxPathX = tree.find { path -> path.bid == placement.bid }!!.segments.maxOf { it.x }
                return@maxOf placement.x + maxPathX
            }

            val placementMaxY = result.maxOf { placement ->
                val maxPathY = tree.find { path -> path.bid == placement.bid }!!.segments.maxOf { it.y }
                return@maxOf placement.y + maxPathY
            }

            val area = placementMaxX * placementMaxY

            return NestResult(result, area)
        }
    }

    private data class NestResult(
        val placement: List<PathPlacement>,
        val fitness: Double,
    )

    private fun applyPlacement(best: List<PathPlacement>): List<Placement> {
        return best.map { pathPlacement ->
            Placement(
                pathPlacement.bid,
                Segment(pathPlacement.x, pathPlacement.y),
                pathPlacement.rotation.toDouble()
            )
        }
    }
}
