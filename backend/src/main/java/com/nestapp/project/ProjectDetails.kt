package com.nestapp.project

import com.nestapp.Configuration
import com.nestapp.minio.ProjectRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun Route.projectDetails(
    configuration: Configuration,
    projectRepository: ProjectRepository,
) {
    get("/project/{project_slug}") {
        val slug = call.parameters["project_slug"] ?: throw NotFoundException("Project not found")

        val projectFiles = projectRepository.getProjectSvgFiles(slug)
            .distinct()

        val response = ProjectDetailsResponse(
            slug = slug,
            name = slug,
            files = projectFiles
                .map { fileName ->
                    ProjectDetailsResponse.ProjectFile(
                        name = fileName,
                        preview = "${configuration.baseUrl}/files/projects/${slug}/files/$fileName.svg",
                    )
                }
        )

        call.respond(HttpStatusCode.OK, response)
    }
}

@Serializable
data class ProjectDetailsResponse(
    @SerialName("slug")
    val slug: String,
    @SerialName("name")
    val name: String,
    @SerialName("files")
    val files: List<ProjectFile>
) {
    @Serializable
    data class ProjectFile(
        @SerialName("name")
        val name: String,
        @SerialName("preview")
        val preview: String,
    )
}
