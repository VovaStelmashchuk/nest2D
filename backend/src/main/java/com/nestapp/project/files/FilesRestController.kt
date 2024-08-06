package com.nestapp.project.files

import io.ktor.server.application.call
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
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
}

