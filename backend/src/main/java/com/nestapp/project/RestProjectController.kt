package com.nestapp.project

import com.nestapp.Configuration
import com.nestapp.mongo.ProjectRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun Route.projectsRestController(
    configuration: Configuration,
    projectRepository: ProjectRepository,
    projectMaker: ProjectMaker,
) {
    post("/project") {
        val multipart = call.receiveMultipart()
        var projectName: String? = null
        var previewFile: ByteArray? = null
        var previewFileNameExtension: String? = null
        val dxfFiles = mutableListOf<Pair<String, ByteArray>>()

        multipart.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    if (part.name == "name") {
                        projectName = part.value
                    }
                }

                is PartData.FileItem -> {
                    val fileName = part.originalFileName ?: throw IllegalArgumentException("File name is not provided")
                    val fileBytes = part.streamProvider().readBytes()
                    when (val fileExtension = fileName.substringAfterLast(".")) {
                        "png", "jpg", "jpeg" -> {
                            if (previewFile != null) {
                                throw IllegalArgumentException("Multiple preview files are not allowed")
                            }
                            previewFile = fileBytes
                            previewFileNameExtension = fileExtension
                        }

                        "dxf" -> {
                            dxfFiles.add(fileName to fileBytes)
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

        val slug = projectMaker.makeProject(projectName!!, previewFile, previewFileNameExtension!!, dxfFiles)

        val response = CreatedProjectResponse(
            slug = slug,
            name = projectName!!
        )

        call.respond(HttpStatusCode.Created, response)
    }

    get("/all_projects") {
        val result = projectRepository.getProjects()
            .map { project ->
                AllProjectsResponse.Project(
                    slug = project.projectSlug,
                    name = project.name,
                    preview = configuration.s3Config.publicUrlStart + project.preview,
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
