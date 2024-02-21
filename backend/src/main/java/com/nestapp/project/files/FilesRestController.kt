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
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.io.File

fun Route.filesRestController(
    previewGenerator: PreviewGenerator,
    projectsRepository: ProjectsRepository,
    projectFilesRepository: ProjectFilesRepository,
    partsRepository: PartsRepository,
) {

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

                    previewGenerator.convertDxfToSvg(file, File(projectFile.svgFilePath))

                    partsRepository.addPartsFromFile(projectFile.id)
                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(HttpStatusCode.Created)
    }
}

