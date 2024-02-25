package com.nestapp.project

import com.nestapp.project.files.ProjectFile
import com.nestapp.project.files.ProjectFilesTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.Locale

class ProjectsRepository {
    init {
        transaction {
            SchemaUtils.create(ProjectsTable)
        }
    }

    fun getProject(slug: ProjectSlug): Project? {
        return transaction {
            Project.find { ProjectsTable.slug eq slug.value }.firstOrNull()
        }
    }

    fun isProjectExists(slug: ProjectSlug): Boolean {
        return transaction {
            Project.find { ProjectsTable.slug eq slug.value }.firstOrNull() != null
        }
    }

    fun addPreview(slug: ProjectSlug, preview: String) {
        transaction {
            ProjectsTable.update({ ProjectsTable.slug eq slug.value }) {
                it[ProjectsTable.preview] = preview
            }
        }
    }

    fun getProjects(): List<Project> {
        return transaction {
            Project.all().toList()
        }
    }

    fun addProject(name: String): Project {
        val project = transaction {
            Project.new {
                this.name = name
                this.slug = createProjectSlug(name).value
            }
        }

        return project
    }

    private fun createProjectSlug(inputString: String): ProjectSlug {
        if (inputString.isBlank()) {
            throw IllegalArgumentException("Project name cannot be blank")
        }
        val filteredString = inputString.filter { it.isLetter() || it.isWhitespace() || it.isDigit() }
        val entityId = filteredString.replace(" ", "-").lowercase(Locale.getDefault())
        return ProjectSlug(entityId)
    }

}

object ProjectsTable : IntIdTable(name = "projects", columnName = "id") {
    val name = text("name")
    val slug = text("slug").uniqueIndex()
    val preview = text("preview").default("")
}

class Project(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Project>(ProjectsTable)

    var name by ProjectsTable.name
    var slug by ProjectsTable.slug
    var preview by ProjectsTable.preview
    val files by ProjectFile referrersOn ProjectFilesTable.projectId
}

object DxfPartsTable : IntIdTable(name = "dxf_parts", columnName = "id") {
    val name = text("name")
    val projectId = reference("project_id", ProjectsTable)
    val fileName = text("file_name")
}

class DatabaseDxfPart(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DatabaseDxfPart>(DxfPartsTable)

    var name by DxfPartsTable.name
    var projectId by DxfPartsTable.projectId
    var fileName by DxfPartsTable.fileName
}

@JvmInline
@Serializable
value class ProjectSlug(val value: String)

@JvmInline
@Serializable
value class FileId(val value: String)
