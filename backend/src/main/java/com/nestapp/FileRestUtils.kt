package com.nestapp

import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun ApplicationCall.fileUploader(
    file: File
) {
    withContext(Dispatchers.IO) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }
    this.receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                val fileBytes = part.streamProvider().readBytes()
                file.writeBytes(fileBytes)
            }

            else -> {}
        }
        part.dispose()
    }
}

suspend fun ApplicationCall.respondFile(file: File) {
    this.response.header(
        HttpHeaders.ContentDisposition,
        ContentDisposition.Attachment.withParameter(
            ContentDisposition.Parameters.FileName,
            file.name,
        ).toString()
    )
    this.respondFile(file)
}
