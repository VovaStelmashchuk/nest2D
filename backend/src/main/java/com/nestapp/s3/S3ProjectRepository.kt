package com.nestapp.s3

import com.amazonaws.services.s3.AmazonS3
import java.io.InputStream

class S3ProjectRepository(
    private val s3Client: AmazonS3,
    private val s3FileUpload: S3FileUpload,
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }


    fun uploadFileToS3ByteArray(bytes: ByteArray, contentType: String, objectName: String) {
        s3FileUpload.uploadFileToS3ByteArray(bytes, contentType, objectName)
    }

    fun getDxfFileAsStream(projectSlug: String, fileName: String): InputStream? {
        println("project slug is $projectSlug and file name is $fileName")
        return try {
            val s3Object = s3Client.getObject(
                BUCKET_NAME,
                "projects/$projectSlug/files/$fileName.dxf"
            )
            s3Object.objectContent
        } catch (e: Exception) {
            println("Error occurred: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
