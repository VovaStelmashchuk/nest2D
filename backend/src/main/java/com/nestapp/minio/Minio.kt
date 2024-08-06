package com.nestapp.minio

import io.minio.MinioClient

class Minio(
    endpoint: String,
    port: Int,
    accessKey: String,
    secretKey: String
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }

    var minioClient: MinioClient = MinioClient.builder()
        .endpoint(endpoint, port, true)
        .credentials(accessKey, secretKey)
        .build()


}
