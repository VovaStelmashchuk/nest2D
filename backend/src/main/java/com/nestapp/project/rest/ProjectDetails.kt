package com.nestapp.project.rest

import com.nestapp.Configuration
import com.nestapp.project.ProjectSlug
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

fun Route.projectDetails(
    configuration: Configuration,
) {
    get("/project/{project_slug}") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw NotFoundException("Project not found"))

        val projectFiles = File(configuration.projectsFolder, slug.value).listFiles().orEmpty()
            .filter { it.isFile }
            .filter { it.extension == "dxf" }

        val response = ProjectDetailsResponse(
            slug = slug,
            name = slug.value,
            files = projectFiles
                .map { file ->
                    ProjectDetailsResponse.ProjectFile(
                        name = file.name,
                        preview = "${configuration.baseUrl}/files/${slug.value}",
                    )
                }
        )

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
