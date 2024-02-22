package com.nestapp.project.rest

import com.nestapp.Configuration
import com.nestapp.project.ProjectSlug
import com.nestapp.project.ProjectsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.projectDetails(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
) {
    get("/project/{project_slug}") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw NotFoundException("Project not found"))
        val project = projectsRepository.getProject(slug) ?: throw NotFoundException("Project not found")

        val response = transaction {
            ProjectDetailsResponse(
                slug = ProjectSlug(project.slug),
                name = project.name,
                files = project.files
                    .map { file ->
                        ProjectDetailsResponse.ProjectFile(
                            name = file.fileName,
                            preview = "${configuration.baseUrl}/files/${project.slug}/${file.fileName}/preview",
                        )
                    }
            )
        }

        call.respond(HttpStatusCode.OK, response)
    }
}

@Serializable
data class ProjectDetailsResponse(
    @SerialName("slug")
    val slug: ProjectSlug,
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
