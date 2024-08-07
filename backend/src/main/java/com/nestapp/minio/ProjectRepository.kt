package com.nestapp.minio

import io.minio.GetObjectArgs
import io.minio.GetObjectResponse
import io.minio.ListObjectsArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.errors.MinioException
import java.io.InputStream

class ProjectRepository(
    private val minioClient: MinioClient
) {

    companion object {
        private const val BUCKET_NAME = "nest2d"
    }


    fun getProjectList(): List<Project> {
        val folderList = mutableListOf<Project>()
        try {
            minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(BUCKET_NAME)
                    .prefix("projects/")
                    .recursive(false)
                    .build()
            ).forEach { result ->
                val objectName = result.get().objectName()
                val projectName = objectName.split("/")[1]
                val preview = "files/$objectName/media/preview.png"

                folderList.add(
                    Project(
                        name = projectName,
                        preview = preview
                    )
                )
            }
        } catch (e: MinioException) {
            println("Error occurred: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return folderList
    }

    fun getProjectSvgFiles(project: String): List<String> {
        val projectFiles = mutableListOf<String>()
        try {
            minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(BUCKET_NAME)
                    .prefix("projects/$project/files")
                    .recursive(true)
                    .build()
            ).forEach { result ->
                val objectName = result.get().objectName()
                val fileName = objectName.split("/").last()

                val fileNameWithoutExtension = fileName.split(".").first()
                projectFiles.add(fileNameWithoutExtension)
            }
        } catch (e: MinioException) {
            println("Error occurred: ${e.message}")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return projectFiles.toList()
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

    data class Project(
        val name: String,
        val preview: String,
    )
}
