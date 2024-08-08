package com.nestapp.minio

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.errors.MinioException
import java.io.InputStream

class MinioProjectRepository(
    private val minioClient: MinioClient,
    private val minioFileUpload: MinioFileUpload,
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }


    fun uploadFileToMinioByteArray(bytes: ByteArray, contentType: String, objectName: String) {
        minioFileUpload.uploadFileToMinioByteArray(bytes, contentType, objectName)
    }

    fun getDxfFileAsStream(projectSlug: String, fileName: String): InputStream? {
        return try {
            minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(BUCKET_NAME)
                    .`object`("projects/$projectSlug/files/$fileName.dxf")
                    .build()
            )
        } catch (e: MinioException) {
            println("Error occurred: ${e.message}")
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
