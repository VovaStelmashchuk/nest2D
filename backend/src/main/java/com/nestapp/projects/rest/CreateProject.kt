package com.nestapp.projects.rest

import com.nestapp.Configuration
import com.nestapp.projects.Project
import com.nestapp.projects.ProjectId
import com.nestapp.projects.ProjectsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import java.util.Locale

fun Route.createProject(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
) {
    post("/project") {
        val request = call.receive<CreateProjectRequest>()
        val project = Project(
            id = createProjectId(request.name),
            name = request.name,
            files = emptyMap(),
        )

        projectsRepository.add(project)

        File(configuration.projectsFolder, project.id.value).mkdirs()

        call.respond(HttpStatusCode.Created, project)
    }
}

fun createProjectId(inputString: String): ProjectId {
    if (inputString.isBlank()) {
        throw IllegalArgumentException("Project name cannot be blank")
    }
    val filteredString = inputString.filter { it.isLetter() || it.isWhitespace() || it.isDigit() }
    val entityId = filteredString.replace(" ", "_").lowercase(Locale.getDefault())
    return ProjectId(entityId)
}

@Serializable
data class CreateProjectRequest(
    @SerialName("name")
    val name: String,
)
