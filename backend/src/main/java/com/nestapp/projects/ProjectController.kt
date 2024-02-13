package com.nestapp.projects

import com.nestapp.Configuration
import com.nestapp.files.SvgFromDxf
import com.nestapp.projects.rest.allProjects
import com.nestapp.projects.rest.createProject
import com.nestapp.projects.rest.projectDetails
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import java.io.File

fun Route.projectsRest(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
    svgFromDxf: SvgFromDxf,
) {
    allProjects(configuration, projectsRepository)
    projectDetails(configuration, projectsRepository)
    createProject(configuration, projectsRepository)

    get("/preview/{project_id}/{file_id}") {
        val project = project(projectsRepository)

        val fileId = FileId(call.parameters["file_id"] ?: throw Exception("file_id not found"))
        val svgFileName = project.files[fileId]?.svgFile ?: throw Exception("file not found")

        val svgFile = File(configuration.projectsFolder, "${project.id.value}/${fileId.value}/${svgFileName}")

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(
                ContentDisposition.Parameters.FileName,
                svgFileName,
            ).toString()
        )
        call.respondFile(svgFile)
    }

    fileUploader(configuration, projectsRepository, svgFromDxf)
}

private fun Route.fileUploader(
    configuration: Configuration,
    projectsRepository: ProjectsRepository,
    svgFromDxf: SvgFromDxf,
) {
    post("/project/{project_id}/add_file") {
        val project = project(projectsRepository)
        val projectFolder = File(configuration.projectsFolder, project.id.value)
        val multipartData = call.receiveMultipart()

        multipartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                }

                is PartData.FileItem -> {
                    val fileName = part.originalFileName as String
                    val fileBytes = part.streamProvider().readBytes()
                    val fileNameNotExtension = fileName.substringBeforeLast(".")
                    val fileId = "$fileNameNotExtension+${project.files.size}"
                    val fileFolder = File(projectFolder, fileId)
                    fileFolder.mkdir()
                    File(fileFolder, fileName).writeBytes(fileBytes)

                    val projectFile = addFileToProject(
                        projectsFolder = configuration.projectsFolder,
                        projectId = project.id,
                        fileId = FileId(fileId),
                        svgFromDxf = svgFromDxf,
                        fileNameNotExtension = fileNameNotExtension,
                    )

                    projectsRepository.addFile(
                        id = project.id,
                        fileId = FileId(fileId),
                        projectFile = projectFile
                    )
                }

                else -> {}
            }
            part.dispose()
        }

        call.respond(HttpStatusCode.Accepted, project(projectsRepository))
    }
}

private fun addFileToProject(
    projectsFolder: File,
    projectId: ProjectId,
    fileId: FileId,
    svgFromDxf: SvgFromDxf,
    fileNameNotExtension: String,
): ProjectFile {
    val fileFolder = File(projectsFolder, "${projectId.value}/${fileId.value}")
    svgFromDxf.convertDxfToSvg(
        File(fileFolder, "${fileNameNotExtension}.dxf"),
        File(fileFolder, "${fileNameNotExtension}.svg"),
    )

    return ProjectFile(
        name = fileNameNotExtension,
        dxfFile = "${fileNameNotExtension}.dxf",
        svgFile = "${fileNameNotExtension}.svg",
    )
}

private fun PipelineContext<Unit, ApplicationCall>.project(
    projectsRepository: ProjectsRepository
): Project {
    val id = ProjectId(call.parameters["project_id"] ?: throw Exception("project_id not found"))
    val project = projectsRepository.getProject(id)
    return project ?: throw Exception("project not found")
}
