package com.nestapp.project

import com.nestapp.Configuration
import com.nestapp.fileUploader
import com.nestapp.project.rest.projectDetails
import com.nestapp.respondFile
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.concurrent.thread

fun Route.projectsRestController(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
) {
    fun ApplicationCall.getProjectSlug(): ProjectSlug {
        val slug = ProjectSlug(this.parameters["project_slug"] ?: throw Exception("project_slug not found"))

        if (!projectsRepository.isProjectExists(slug)) {
            throw NotFoundException("Project not found")
        }
        return slug
    }
    post("/project") {
        val request = call.receive<CreateProjectRequest>()
        val project = projectsRepository.addProject(request.name)
        File(configuration.projectsFolder, project.slug).mkdirs()

        val response = CreatedProject(
            slug = project.slug,
            name = project.name,
        )

        call.respond(HttpStatusCode.Created, response)
    }

    post("/project/{project_slug}/preview") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw Exception("project_slug not found"))
        val file = File(configuration.projectsFolder, "${slug.value}/media/preview.png")
        call.fileUploader(file)
        projectsRepository.addPreview(slug, file.path)
        call.respond(HttpStatusCode.Created)
    }

    get("/project/{project_slug}/preview") {
        val slug = call.getProjectSlug()

        val file = File(configuration.projectsFolder, "${slug.value}/media/preview.png")
        call.respondFile(file)
    }

    get("/all_projects") {
        val result = projectsRepository.getProjects()
            .map { project ->
                AllProjectsResponse.Project(
                    slug = ProjectSlug(project.slug),
                    name = project.name,
                    preview = "${configuration.baseUrl}/project/${project.slug}/preview",
                )
            }
        call.respond(HttpStatusCode.OK, result)
    }

    get("/project/{project_slug}/preview") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw Exception("project_slug not found"))
        val project = projectsRepository.getProject(slug)
        if (project != null) {
            val file = File(project.preview)
            call.respondFile(file)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    projectDetails(configuration, projectsRepository)
}

@Serializable
data class AllProjectsResponse(
    @SerialName("project")
    val project: List<Project>
) {
    @Serializable
    data class Project(
        @SerialName("slug")
        val slug: ProjectSlug,
        @SerialName("name")
        val name: String,
        @SerialName("preview")
        val preview: String,
    )
}

@Serializable
data class CreateProjectRequest(
    @SerialName("name")
    val name: String,
)

@Serializable
data class CreatedProject(
    val slug: String,
    val name: String,
)
