package com.nestapp.minio

import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.MinioException

class MinioFileUpload(
    private val minioClient: MinioClient,
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }

    fun uploadFileToMinioByteArray(bytes: ByteArray, contentType: String, objectName: String) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .`object`(objectName)
                    .stream(bytes.inputStream(), bytes.size.toLong(), -1)
                    .contentType(contentType)
                    .build()
            )
        } catch (e: MinioException) {
            println("Error occurred: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
