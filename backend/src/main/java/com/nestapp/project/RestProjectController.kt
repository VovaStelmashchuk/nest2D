package com.nestapp.project

import com.nestapp.Configuration
import com.nestapp.minio.ProjectRepository
import com.nestapp.project.rest.projectDetails
import com.nestapp.respondFile
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.Locale

fun Route.projectsRestController(
    configuration: Configuration,
    projectRepository: ProjectRepository,
) {
    fun createProjectSlug(inputString: String): String {
        if (inputString.isBlank()) {
            throw IllegalArgumentException("Project name cannot be blank")
        }
        val filteredString = inputString.filter { it.isLetter() || it.isWhitespace() || it.isDigit() }
        val slug = filteredString.replace(" ", "-").lowercase(Locale.getDefault())
        return slug
    }

    post("/project") {
        val multipart = call.receiveMultipart()
        var projectName: String? = null
        var previewFile: File? = null
        val dxfFiles = mutableListOf<File>()

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "name") {
                        projectName = part.value
                    }
                }

                is PartData.FileItem -> {
                    val fileName = part.originalFileName ?: throw IllegalArgumentException("File name is not provided")
                    val fileExtension = fileName.substringAfterLast(".")
                    val slug = projectName?.let { createProjectSlug(it) }
                        ?: throw IllegalArgumentException("Project name is required")

                    val projectFolder = File(configuration.projectsFolder, slug)
                    if (!projectFolder.exists()) {
                        projectFolder.mkdirs()
                    }

                    when (fileExtension) {
                        "png", "jpg", "jpeg" -> {
                            if (previewFile != null) {
                                throw IllegalArgumentException("Multiple preview files are not allowed")
                            }
                            previewFile = File(projectFolder, "media/preview.$fileExtension").apply {
                                parentFile.mkdirs()
                                createNewFile()
                                writeBytes(part.streamProvider().readBytes())
                            }
                        }

                        "dxf" -> {
                            val dxfFile = File(projectFolder, "files/$fileName").apply {
                                parentFile.mkdirs()
                                createNewFile()
                                writeBytes(part.streamProvider().readBytes())
                            }
                            dxfFiles.add(dxfFile)
                        }

                        else -> {
                            throw IllegalArgumentException("Unsupported file type: $fileExtension")
                        }
                    }
                }

                else -> Unit
            }
            part.dispose()
        }

        if (projectName == null || dxfFiles.isEmpty()) {
            throw IllegalArgumentException("Project name and at least one DXF file are required")
        }

        val response = CreatedProjectResponse(
            slug = createProjectSlug(projectName!!),
            name = projectName!!,
        )

        call.respond(HttpStatusCode.Created, response)
    }

    get("/all_projects") {
        val result = projectRepository.getProjectList()
            .map { project ->
                AllProjectsResponse.Project(
                    slug = project.name,
                    name = project.name,
                    preview = configuration.baseUrl + project.preview,
                )
            }
        call.respond(HttpStatusCode.OK, result)
    }

    projectDetails(configuration, projectRepository)
}

@Serializable
data class AllProjectsResponse(
    @SerialName("project")
    val project: List<Project>
) {
    @Serializable
    data class Project(
        @SerialName("slug")
        val slug: String,
        @SerialName("name")
        val name: String,
        @SerialName("preview")
        val preview: String,
    )
}

@Serializable
data class CreatedProjectResponse(
    val slug: String,
    val name: String,
)
