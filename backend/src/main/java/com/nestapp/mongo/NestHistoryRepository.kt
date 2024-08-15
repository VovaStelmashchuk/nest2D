package com.nestapp.mongo

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

class NestHistoryRepository(
    private val client: MongoClient
) {

    private val database by lazy { client.getDatabase("nest2d") }

    suspend fun createNestResult(projectSlug: String): NestResultDatabase {
        val collection = database.getCollection<NestResultDatabase>(collectionName = "nest-history")
        val nestResult = NestResultDatabase(
            id = ObjectId(),
            projectSlug = projectSlug,
            startAt = Date(),
            endAt = null,
            status = IN_PROGRESS
        )
        collection.insertOne(nestResult)
        return nestResult
    }

    suspend fun makeNestFinish(id: ObjectId, svgPath: String, dxfPath: String) {
        val collection = database.getCollection<NestResultDatabase>(collectionName = "nest-history")
        val query = Filters.eq("_id", id)

        val update = org.bson.Document(
            "\$set", org.bson.Document("status", DONE)
                .append("endAt", Date())
                .append("svgPreviewUrl", svgPath)
                .append("dxfFileUrl", dxfPath)
        )

        collection.updateOne(query, update)
    }
}

data class NestResultDatabase(
    @BsonId
    val id: ObjectId,
    val projectSlug: String,
    val startAt: Date,
    val endAt: Date?,
    val status: String,
    val svgPreviewUrl: String? = null,
    val dxfFileUrl: String? = null,
)

const val IN_PROGRESS = "IN_PROGRESS"
const val DONE = "DONE"
const val FAIL = "FAIL"

