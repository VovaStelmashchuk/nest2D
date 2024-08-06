package com.nestapp

import com.nestapp.minio.ProjectRepository
import io.minio.MinioClient

class AppComponent(
    val configuration: Configuration,
) {

    private var minioClient: MinioClient = MinioClient.builder()
        .endpoint(configuration.endpoint, configuration.port, false)
        .credentials(configuration.accessKey, configuration.secretKey)
        .build()

    val projectRepository: ProjectRepository = ProjectRepository(minioClient)

}
