package com.nestapp

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.nestapp.minio.MinioFileUpload
import com.nestapp.minio.MinioProjectRepository
import com.nestapp.mongo.NestHistoryRepository
import com.nestapp.mongo.ProjectDatabase
import com.nestapp.mongo.ProjectRepository
import io.minio.MinioClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId

class AppComponent(
    val configuration: Configuration,
) {

    private var minioClient: MinioClient = MinioClient.builder()
        .endpoint(configuration.endpoint, configuration.port, false)
        .credentials(configuration.accessKey, configuration.secretKey)
        .build()

    val minioFileUpload = MinioFileUpload(minioClient)

    val minioProjectRepository: MinioProjectRepository = MinioProjectRepository(minioClient, minioFileUpload)

    private val mongoClient = MongoClient.create(connectionString = configuration.mongoUrl)

    val nestHistoryRepository = NestHistoryRepository(mongoClient)

    val projectRepository = ProjectRepository(mongoClient)

}
