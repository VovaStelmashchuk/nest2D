package com.nestapp.nest

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Placement
import com.nestapp.nest.data.Result
import com.nestapp.nest.data.Segment
import com.nestapp.nest.nfp.NfpCacheRepository
import com.nestapp.nest.util.PlacementWorker

class Nest {
    private val nfpCache = NfpCacheRepository()

    fun startNest(
        binPolygon: NestPath,
        tree: List<NestPath>
    ): List<Placement>? {
        nfpCache.setNestPaths(tree)
        nfpCache.setBinPolygon(binPolygon)

        var best: Result? = null

        val variants: List<List<NestPath?>> = generateNestListVariants(tree)

        println("startNest(): variants.size() = " + variants.size)

        for (variant in variants) {
            val worker = PlacementWorker(binPolygon, nfpCache)
            val result = worker.placePaths(variant)

            if (result == null) {
                println("resul null cannot place paths")
            } else {
                println("startNest(): current fitness = " + result.fitness)

                if (best == null || best.fitness > result.fitness) {
                    best = result
                }
            }
        }

        if (best == null) {
            return null
        } else {
            println("startNest(): best fitness = " + best.fitness)
            return applyPlacement(best)
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
