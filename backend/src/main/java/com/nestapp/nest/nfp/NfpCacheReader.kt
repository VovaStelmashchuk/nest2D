package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Segment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction

class NfpCacheReader(
    private val json: Json,
) {

    private val cache: MutableMap<NfpKey, List<NestPath>> = HashMap()

    fun get(nfpKey: NfpKey): List<NestPath> {
        if (cache.containsKey(nfpKey)) {
            return cache[nfpKey]!!
        }
        val databaseResult = transaction {
            NfpCacheDatabase.findById(json.encodeToString(nfpKey))
        }

        databaseResult ?: throw IllegalStateException("NfpCacheRepository.get: $nfpKey not found")

        val nestPaths = databaseResult.nfp.split("|").map { path ->
            val nestPath = NestPath()
            path.split(";").map {
                val (x, y) = it.split(",")
                nestPath.add(Segment(x.toDouble(), y.toDouble()))
            }
            nestPath
        }

        return nestPaths.also {
            cache[nfpKey] = it
        }
    }
}
