package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.NfpUtil
import io.ktor.util.logging.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class NfpCacheRepository(
    private val nestPaths: List<NestPath>,
    private val logger: Logger,
) {

    private val nfpCache: MutableMap<NfpKey, List<NestPath>> = ConcurrentHashMap()

    private val executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
            .also {
                logger.info("NfpCacheRepository: availableProcessors = $it")
            }
    )

    fun prepareCacheForKeys(keys: List<NfpKey>) {
        val notExistKeys = keys.minus(nfpCache.keys)
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

        val a = nestPaths
            .find { it.bid == key.a } ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")

        val b =
            nestPaths
                .find { it.bid == key.b } ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")

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
