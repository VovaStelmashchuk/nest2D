package com.nestapp.mongo

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.Date

class ProjectRepository(
    private val client: MongoClient
) {

    private val database by lazy { client.getDatabase("nest2d") }

    suspend fun insertProject(projectDatabase: ProjectDatabase) {
        val collection = database.getCollection<ProjectDatabase>(collectionName = "projects")
        collection.insertOne(projectDatabase)
    }

    suspend fun getProjects(): List<ProjectDatabase> {
        val collection = database.getCollection<ProjectDatabase>(collectionName = "projects")
        return collection.find()
            .sort(Sorts.descending("createdAt"))
            .toList()
    }

    suspend fun getProject(slug: String): ProjectDatabase {
        val collection = database.getCollection<ProjectDatabase>(collectionName = "projects")
        val query = Filters.eq("projectSlug", slug)
        return collection.find(query).firstOrNull()
            ?: throw IllegalArgumentException("Project not found $slug")
    }
}

data class ProjectDatabase(
    @BsonId
    val id: ObjectId,
    val projectSlug: String,
    val name: String,
    val files: List<String>,
    val preview: String?,
    val createdAt: Date?,
)

