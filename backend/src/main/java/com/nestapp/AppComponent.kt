package com.nestapp

import com.mongodb.kotlin.client.coroutine.MongoClient
import com.nestapp.minio.MinioFileUpload
import com.nestapp.minio.ProjectRepository
import com.nestapp.mongo.NestHistoryRepository
import io.minio.MinioClient

class AppComponent(
    val configuration: Configuration,
) {

    private var minioClient: MinioClient = MinioClient.builder()
        .endpoint(configuration.endpoint, configuration.port, false)
        .credentials(configuration.accessKey, configuration.secretKey)
        .build()

    val minioFileUpload = MinioFileUpload(minioClient)

    val projectRepository: ProjectRepository = ProjectRepository(minioClient, minioFileUpload)

    private val mongoClient = MongoClient.create(connectionString = configuration.mongoUrl)

    val nestHistoryRepository = NestHistoryRepository(mongoClient)

}
