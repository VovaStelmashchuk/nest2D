package com.nestapp.s3

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import java.io.ByteArrayInputStream

class S3FileUpload(
    private val s3Client: AmazonS3,
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }

    fun uploadFileToS3ByteArray(bytes: ByteArray, contentType: String, objectName: String) {
        try {
            val metadata = ObjectMetadata().apply {
                contentLength = bytes.size.toLong()
                this.contentType = contentType
            }

            s3Client.putObject(
                BUCKET_NAME,
                objectName,
                ByteArrayInputStream(bytes),
                metadata
            )
        } catch (e: Exception) {
            println("Error occurred: ${e.message}")
            e.printStackTrace()
        }
    }
}

