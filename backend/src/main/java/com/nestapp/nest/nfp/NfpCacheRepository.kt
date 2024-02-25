package com.nestapp.nest.nfp

import com.nestapp.nest.data.NestPath
import com.nestapp.nest.data.Segment
import com.nestapp.nest.nfp.NestPathTable.entityId
import com.nestapp.nest.nfp.NfpCacheTable.uniqueIndex
import com.nestapp.nest.util.NfpUtil
import io.ktor.util.logging.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap

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
            NestPathTable.select(NestPathTable.id)
                .where { NestPathTable.id inList distinctBids }
                .map { it[NestPathTable.id].value }
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

    private fun getNestPathByBid(bid: String): NestPath {
        return transaction {
            val databasePath = NestPathDatabase.findById(bid) ?: throw IllegalArgumentException("NestPath not found")

            val nestPath = NestPath(bid)
            databasePath.segments.split(";").map {
                val (x, y) = it.split(",")
                nestPath.add(Segment(x.toDouble(), y.toDouble()))
            }

            nestPath
        }
    }

    fun prepareCacheForKeys(keys: List<NfpKey>) {
        val keysInDatabaseFormat = keys.map {
            json.encodeToString(it)
        }

        val existedKeys = transaction {
            NfpCacheDatabase.find { NfpCacheTable.id inList keysInDatabaseFormat }
                .map { it.id.value }
                .map { json.decodeFromString<NfpKey>(it) }
        }

        keys.minus(existedKeys.toSet()).map { key ->
            generateNfp(key).also { data ->
                transaction {
                    NfpCacheDatabase.new(json.encodeToString(key)) {
                        nfp = data.joinToString("|") { nestPath ->
                            nestPath.segments.joinToString(";") { segment ->
                                "${segment.x},${segment.y}"
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateNfp(key: NfpKey): List<NestPath> {
        logger.info("NfpCacheRepository.generateNfp: key = $key")

        val a = getNestPathByBid(key.aBid)
        val b = getNestPathByBid(key.bBid)

        val nfpPair = NfpPair(
            a = a,
            b = b,
            key = key,
        )

        return NfpUtil.nfpGenerator(nfpPair) ?: throw IllegalArgumentException("Cannot generate NFP for key: $key")
    }

    fun get(nfpKey: NfpKey): List<NestPath> {
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

        return nestPaths
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


