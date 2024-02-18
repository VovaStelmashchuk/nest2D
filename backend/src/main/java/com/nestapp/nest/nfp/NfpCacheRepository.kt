package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.NfpUtil
import io.ktor.util.logging.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class NfpCacheRepository(
    private val logger: Logger,
) {

    private val nestPaths: MutableMap<String, NestPath> = ConcurrentHashMap()

    private val nfpCache: MutableMap<NfpKey, List<NestPath>> = ConcurrentHashMap()

    private val executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
            .also {
                logger.info("NfpCacheRepository: availableProcessors = $it")
            }
    )

    fun addNestPaths(nestPaths: List<NestPath>) {
        nestPaths.distinctBy { it.bid }.forEach { path ->
            if (!this.nestPaths.containsKey(path.bid)) {
                this.nestPaths[path.bid] = path
            }
        }
    }

    fun prepareCacheForKeys(keys: List<NfpKey>) {
        val notExistKeys = keys.toSet().minus(nfpCache.keys)
        val futures = notExistKeys.map { key ->
            CompletableFuture.supplyAsync({
                generateNfp(key).also { data ->
                    nfpCache[key] = data
                }
            }, executor)
        }
        // Wait for all futures to complete
        CompletableFuture.allOf(*futures.toTypedArray()).join()
    }

    private fun generateNfp(key: NfpKey): List<NestPath> {
        logger.info("NfpCacheRepository.generateNfp: key = $key")

        val a = nestPaths[key.a] ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")
        val b = nestPaths[key.b] ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")

        val nfpPair = NfpPair(
            a = a,
            b = b,
            key = key,
        )

        return NfpUtil.nfpGenerator(nfpPair) ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")
    }

    fun get(nfpKey: NfpKey): List<NestPath> {
        return nfpCache[nfpKey] ?: throw IllegalStateException("NfpCacheRepository.get: nfpKey not found")
    }
}
