package com.nestapp.projects.rest

import com.nestapp.Configuration
import com.nestapp.projects.FileId
import com.nestapp.projects.Project
import com.nestapp.projects.ProjectId
import com.nestapp.projects.ProjectsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun Route.projectDetails(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
) {
    get("/project/{project_id}") {
        val project = project(projectsRepository)
        val response = ProjectDetailsResponse(
            id = project.id,
            name = project.name,
            files = project.files
                .map { (key, projectFile) ->
                    ProjectDetailsResponse.ProjectFile(
                        id = key,
                        name = projectFile.name,
                        svgUrl = "${configuration.baseUrl}/preview/${project.id.value}/${key.value}",
                    )
                }
        )
        call.respond(HttpStatusCode.OK, response)
    }
}

@Serializable
data class ProjectDetailsResponse(
    @SerialName("id")
    val id: ProjectId,
    @SerialName("name")
    val name: String,
    @SerialName("files")
    val files: List<ProjectFile>
) {
    @Serializable
    data class ProjectFile(
        @SerialName("id")
        val id: FileId,
        @SerialName("name")
        val name: String,
        @SerialName("svg_url")
        val svgUrl: String,
    )
}

private fun PipelineContext<Unit, ApplicationCall>.project(
    projectsRepository: ProjectsRepository
): Project {
    val id = ProjectId(call.parameters["project_id"] ?: throw Exception("project_id not found"))
    val project = projectsRepository.getProject(id)
    return project ?: throw Exception("project not found")
}
