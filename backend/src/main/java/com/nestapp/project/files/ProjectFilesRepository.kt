package com.nestapp.project.files

import com.nestapp.project.ProjectsTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

class ProjectFilesRepository {

    fun getFiles(projectId: Int): List<ProjectFile> {
        return ProjectFile.find { ProjectFilesTable.projectId eq projectId }.toList()
    }

    fun addFile(projectId: Int, file: String): ProjectFile {
        return ProjectFile.new {
            this.projectId = EntityID(projectId, ProjectsTable)
            this.file = file
        }
    }
}


object ProjectFilesTable : IntIdTable(name = "project_files", columnName = "id") {
    val projectId = reference("project_id", ProjectsTable)
    val file = text("file")
}

class ProjectFile(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProjectFile>(ProjectFilesTable)

    var projectId by ProjectFilesTable.projectId
    var file by ProjectFilesTable.file
}
