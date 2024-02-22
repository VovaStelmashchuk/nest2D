package com.nestapp.project.files

import com.nestapp.Configuration
import com.nestapp.project.Project
import com.nestapp.project.ProjectsTable
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class ProjectFilesRepository(
    private val configuration: Configuration,
) {

    init {
        transaction {
            SchemaUtils.create(ProjectFilesTable)
        }
    }

    fun getFile(projectSlug: String, fileName: String): ProjectFile {
        return transaction {
            val project = Project.find { ProjectsTable.slug eq projectSlug }.first()
            return@transaction ProjectFile.find {
                ProjectFilesTable.projectId eq project.id and (ProjectFilesTable.fileName eq fileName)
            }.firstOrNull() ?: throw NotFoundException()
        }
    }

    fun addFile(projectSlug: String, fileNameWithoutExtension: String): ProjectFile {
        return transaction {
            val project = Project.find { ProjectsTable.slug eq projectSlug }.first()

            val file = ProjectFile.find {
                ProjectFilesTable.projectId eq project.id and (ProjectFilesTable.fileName eq fileNameWithoutExtension)
            }.firstOrNull()

            val folderPath = "${configuration.projectsFolder}/$projectSlug/files"

            if (file == null) {
                ProjectFile.new {
                    this.projectId = project.id
                    this.fileName = fileNameWithoutExtension
                    this.dxfFilePath = "$folderPath/$fileNameWithoutExtension.dxf"
                    this.svgFilePath = "$folderPath/$fileNameWithoutExtension.svg"
                }
            } else {
                addFile(projectSlug, "${fileNameWithoutExtension}_copy")
            }
        }
    }
}


object ProjectFilesTable : IntIdTable(name = "project_files", columnName = "id") {
    val projectId = reference("project_id", ProjectsTable)
    val fileName = text("file_name").uniqueIndex()
    val dxfFilePath = text("dxf_file_path")
    val svgFilePath = text("svg_file_path")
}

class ProjectFile(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProjectFile>(ProjectFilesTable)

    var projectId by ProjectFilesTable.projectId
    var fileName by ProjectFilesTable.fileName
    var dxfFilePath by ProjectFilesTable.dxfFilePath
    var svgFilePath by ProjectFilesTable.svgFilePath
}
