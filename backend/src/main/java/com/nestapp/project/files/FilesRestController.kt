package com.nestapp.project.files

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

fun Route.filesRestController() {
    get("files/{file_path...}") {
        val filePath = call.parameters["file_path"] ?: throw NotFoundException()
        val file = File("mount/projects/$filePath")
        if (!file.exists()) {
            throw NotFoundException()
        }

        call.respondFile(file)
    }

    post("files/{project_slug}/dxf") {
        val slug = call.parameters["project_slug"] ?: throw NotFoundException()

        call.receiveMultipart().forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val originFileName =
                        part.originalFileName ?: throw IllegalArgumentException("File name is not provided")
                    val fileExtension = originFileName.substringAfterLast(".")
                    if (fileExtension != "dxf") {
                        throw IllegalArgumentException("File extension is not supported")
                    }

                    val projectFolder = File("mount/projects/$slug")
                    if (!projectFolder.exists()) {
                        projectFolder.mkdirs()
                    }
                    // create sub folder 'files' if not exists
                    File(projectFolder, "files").mkdirs()

                    // save request file into files folder
                    val file = File(projectFolder, "files/$originFileName")
                    file.createNewFile()
                    file.writeBytes(part.streamProvider().readBytes())
                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(HttpStatusCode.Created)
    }
}

