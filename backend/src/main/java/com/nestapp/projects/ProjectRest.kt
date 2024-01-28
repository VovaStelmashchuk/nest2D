package com.nestapp.projects

import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.io.File

private const val laser_gridfinity_id = "laser_gridfinity_boxes_0"

fun Route.projectRest(mountFolder: File, projectsRepository: ProjectsRepository) {
    get("/project") {
        val project = projectsRepository.getProject(ProjectId(laser_gridfinity_id))
        print(project)
        call.respond(HttpStatusCode.OK, project ?: throw Exception("project not found"))
    }

    get("/preview/{project_id}/{file_id}") {
        val projectId = ProjectId(call.parameters["project_id"] ?: throw Exception("project_id not found"))
        val fileId = FileId(call.parameters["file_id"] ?: throw Exception("file_id not found"))

        val project = projectsRepository.getProject(projectId) ?: throw Exception("project not found")
        val svgFileName = project.files[fileId]?.svgFile ?: throw Exception("file not found")

        val svgFile = File(mountFolder, "user_inputs/${fileId.value}/${svgFileName}")

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                svgFileName,
            ).toString()
        )
        call.respondFile(svgFile)
    }
}
