package com.nestapp.projects.rest

import com.nestapp.Configuration
import com.nestapp.projects.ProjectId
import com.nestapp.projects.ProjectsRepository
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

fun Route.allProjects(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
) {
    get("/all_projects") {
        val result = projectsRepository.getProjects()
            .map { (_, project) ->
                AllProjectsResponse.Project(
                    id = project.id,
                    name = project.name,
                    preview = "${configuration.baseUrl}/project/${project.id.value}/preview",
                )
            }
        call.respond(HttpStatusCode.OK, result)
    }

    get("/project/{project_id}/preview") {
        val projectId = ProjectId(call.parameters["project_id"] ?: throw Exception("project_id not found"))

        val svgFile = File(configuration.projectsFolder, "${projectId.value}/media/preview.png")

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                "preview.png",
            ).toString()
        )
        call.respondFile(svgFile)
    }
}

@Serializable
data class AllProjectsResponse(
    @SerialName("project")
    val project: List<Project>
) {
    @Serializable
    data class Project(
        @SerialName("id")
        val id: ProjectId,
        @SerialName("name")
        val name: String,
        @SerialName("preview")
        val preview: String,
    )
}

