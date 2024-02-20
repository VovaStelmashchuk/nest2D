package com.nestapp.project.files

import com.nestapp.Configuration
import com.nestapp.fileUploader
import com.nestapp.project.ProjectSlug
import com.nestapp.project.ProjectsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

fun Route.filesRestController(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
    projectFilesRepository: ProjectFilesRepository,
) {

    post("files/{project_slug}/add_dxf_file") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw NotFoundException())
        val project = projectsRepository.getProject(slug) ?: throw NotFoundException()

        val file = File(configuration.projectsFolder, "${slug.value}/files/${project.id.value}.dxf")

        projectFilesRepository.addFile(
            projectId = project.id.value,
            file = file.absolutePath,
        )
    }
}

