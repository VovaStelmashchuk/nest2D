package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Segment
import com.nestapp.nest.util.NfpUtil
import io.ktor.util.logging.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class NfpCacheRepository(
    private val logger: Logger,
    private val json: Json,
) {

    init {
        transaction {
            SchemaUtils.create(NestPathTable, NfpCacheTable)
        }
    }

    fun addNestPaths(nestPaths: List<NestPath>) {
        val distinctBids = nestPaths.map { it.bid }.distinct()

        val existIds = transaction {
            NestPathDatabase.forIds(distinctBids)
                .map { it.id.value }
        }

        val notExistsIds: List<String> = distinctBids.minus(existIds.toSet())

        notExistsIds.forEach { id: String ->
            val path = nestPaths.first { it.bid == id }
            transaction {
                NestPathTable.insert {
                    it[NestPathTable.id] = id
                    it[NestPathTable.segments] = path.segments
                        .joinToString(separator = ";") { segment ->
                            "${segment.x},${segment.y}"
                        }
                }
            }
        }
    }

    private fun getNestPathsByBids(bids: List<String>): Map<String, NestPath> {
        return NestPathDatabase.forIds(bids)
            .map {
                val nestPath = NestPath(it.id.value)
                it.segments.split(";").map {
                    val (x, y) = it.split(",")
                    nestPath.add(Segment(x.toDouble(), y.toDouble()))
                }
                nestPath
            }
            .associateBy { it.bid }

    }

    fun prepareCacheForKeys(keys: List<NfpKey>) {
        val keysInDatabaseFormat = keys.toSet().map {
            json.encodeToString(it)
        }.distinct()

        val existedKeys = transaction {
            NfpCacheDatabase
                .find { NfpCacheTable.id inList keysInDatabaseFormat }
                .map { it.id.value }
                .map { json.decodeFromString<NfpKey>(it) }
        }

        val keyWithoutNfpCache = keys.minus(existedKeys.toSet()).toSet()

        val allNestPathIdsNeedForNfpGeneration = keyWithoutNfpCache.flatMap { key ->
            listOf(key.aBid, key.bBid)
        }.distinct()

        transaction {
            val nestPaths = getNestPathsByBids(allNestPathIdsNeedForNfpGeneration)
            keyWithoutNfpCache.map { key ->
                NfpCacheDatabase.new(id = json.encodeToString(key)) {
                    this.nfp = generateNfp(key, nestPaths).joinToString("|") { path ->
                        path.segments.joinToString(";") { segment ->
                            "${segment.x},${segment.y}"
                        }
                    }
                }
            }
        }
    }

    private fun generateNfp(key: NfpKey, nestPaths: Map<String, NestPath>): List<NestPath> {
        logger.info("NfpCacheRepository.generateNfp: key = $key")

        val a = nestPaths[key.aBid] ?: throw IllegalArgumentException("NestPath not found for bid: ${key.aBid}")
        val b = nestPaths[key.bBid] ?: throw IllegalArgumentException("NestPath not found for bid: ${key.bBid}")

        val nfpPair = NfpPair(
            a = a,
            b = b,
            key = key,
        )

        return NfpUtil.nfpGenerator(nfpPair) ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")
    }
}

object NestPathTable : IdTable<String>(name = "nest_paths") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId().uniqueIndex()

    //The format of segments is "x1,y1;x2,y2;x3,y3;..."
    val segments = text("segments")
}

class NestPathDatabase(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, NestPathDatabase>(NestPathTable)

    var segments by NestPathTable.segments

}

object NfpCacheTable : IdTable<String>(name = "nfp_cache") {
    override val id: Column<EntityID<String>> = varchar("id", 255).entityId().uniqueIndex()
    val nfp = text("nfp")
}

class NfpCacheDatabase(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, NfpCacheDatabase>(NfpCacheTable)

    //The format of nfp is "x11,y11;x12,y12;x13,y13;|x21,y21;x22,y22;x23,y23;|..."
    var nfp by NfpCacheTable.nfp
}
