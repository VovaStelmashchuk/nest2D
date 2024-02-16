package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.util.NfpUtil

class NfpCacheRepository {

    private val nfpCache: MutableMap<NfpKey, List<NestPath>> = HashMap()

    private lateinit var nestPaths: List<NestPath>
    private lateinit var binPolygon: NestPath

    fun setNestPaths(nestPaths: List<NestPath>) {
        this.nestPaths = nestPaths
    }

    fun setBinPolygon(binPolygon: NestPath) {
        this.binPolygon = binPolygon
    }

    fun exist(nfpKey: NfpKey): Boolean {
        if (nfpCache.containsKey(nfpKey)) {
            println("NFP exist in cache: $nfpKey")
            return true
        } else {
            val a = if (nfpKey.a == -1) {
                binPolygon
            } else {
                nestPaths.find { it.bid == nfpKey.a } ?: return false
            }

            val b = if (nfpKey.b == -1) {
                binPolygon
            } else {
                nestPaths.find { it.bid == nfpKey.b } ?: return false
            }

            val nfpPair = NfpPair(
                a = a,
                b = b,
                key = nfpKey,
            )

            val data = NfpUtil.nfpGenerator(nfpPair) ?: return false
            nfpCache[nfpKey] = data.value
            println("Add NFP to cache: $nfpKey")
            return true
        }
    }

    fun get(nfpKey: NfpKey): List<NestPath> {
        return nfpCache[nfpKey] ?: throw IllegalStateException("NfpCacheRepository.get: nfpKey not found")
    }

}
