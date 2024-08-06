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
import java.util.Locale

fun Route.projectsRestController(
    configuration: Configuration,
) {
    fun createProjectSlug(inputString: String): String {
        if (inputString.isBlank()) {
            throw IllegalArgumentException("Project name cannot be blank")
        }
        val filteredString = inputString.filter { it.isLetter() || it.isWhitespace() || it.isDigit() }
        val slug = filteredString.replace(" ", "-").lowercase(Locale.getDefault())
        return slug
    }

    fun ApplicationCall.getProjectSlug(): ProjectSlug {
        return ProjectSlug(this.parameters["project_slug"] ?: throw Exception("project_slug not found"))
    }

    post("/project") {
        val request = call.receive<CreateProjectRequest>()
        val slug = createProjectSlug(request.name)
        File(configuration.projectsFolder, slug).mkdirs()

        val response = CreatedProjectResponse(
            slug = slug,
            name = slug,
        )

        call.respond(HttpStatusCode.Created, response)
    }

    post("/project/{project_slug}/preview") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw Exception("project_slug not found"))
        val file = File(configuration.projectsFolder, "${slug.value}/media/preview.png")
        call.fileUploader(file)
        call.respond(HttpStatusCode.Created)
    }

    get("/project/{project_slug}/preview") {
        val slug = call.getProjectSlug()

        val file = File(configuration.projectsFolder, "${slug.value}/media/preview.png")
        call.respondFile(file)
    }

    get("/all_projects") {
        val result = configuration.projectsFolder.list().orEmpty()
            .map { projectFolderName ->
                AllProjectsResponse.Project(
                    slug = ProjectSlug(projectFolderName),
                    name = projectFolderName,
                    preview = "${configuration.baseUrl}/project/${projectFolderName}/preview",
                )
            }
        call.respond(HttpStatusCode.OK, result)
    }

    projectDetails(configuration)
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
data class CreatedProjectResponse(
    val slug: String,
    val name: String,
)
