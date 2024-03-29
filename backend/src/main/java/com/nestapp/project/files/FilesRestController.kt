package com.nestapp.project.files

import com.nestapp.files.PreviewGenerator
import com.nestapp.project.ProjectSlug
import com.nestapp.project.ProjectsRepository
import com.nestapp.project.parts.PartsRepository
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.io.File

fun Route.filesRestController(
    previewGenerator: PreviewGenerator,
    projectsRepository: ProjectsRepository,
    projectFilesRepository: ProjectFilesRepository,
    partsRepository: PartsRepository,
) {
    get("preview/{project_slug}/{file_name}") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw NotFoundException())
        val project = projectsRepository.getProject(slug) ?: throw NotFoundException()
        val fileName = call.parameters["file_name"] ?: throw NotFoundException()

        val projectFile = projectFilesRepository.getFile(project.slug, fileName)

        val file = File(projectFile.svgFilePath)
        if (!file.exists()) {
            throw NotFoundException()
        }

        call.respondFile(file)
    }

    post("files/{project_slug}/dxf") {
        val slug = ProjectSlug(call.parameters["project_slug"] ?: throw NotFoundException())
        val project = projectsRepository.getProject(slug) ?: throw NotFoundException()

        call.receiveMultipart().forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val originFileName =
                        part.originalFileName ?: throw IllegalArgumentException("File name is not provided")
                    val fileExtension = originFileName.substringAfterLast(".")
                    if (fileExtension != "dxf") {
                        throw IllegalArgumentException("File extension is not supported")
                    }
                    val fileNameWithoutExtension = originFileName.substringBeforeLast(".")

                    val projectFile = projectFilesRepository.addFile(
                        projectSlug = project.slug,
                        fileNameWithoutExtension = fileNameWithoutExtension,
                    )

                    val fileBytes = part.streamProvider().readBytes()
                    val file = File(projectFile.dxfFilePath)
                    file.parentFile.mkdirs()
                    file.createNewFile()
                    file.writeBytes(fileBytes)

                    partsRepository.addPartsFromFile(projectFile.id)

                    previewGenerator.createFilePreview(projectFile.id.value, File(projectFile.svgFilePath))
                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(HttpStatusCode.Created)
    }
}

