package com.nestapp.projects

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

class ProjectsRepository(
    private val json: Json,
) {

    companion object {
        private const val FILE = "mount/app_data/projects.json"
    }

    fun getProject(id: ProjectId): Project? {
        val root = getProjectRoot()
        return root.projects[id]
    }

    fun getFiles(id: ProjectId, fileIds: List<FileId>): Map<FileId, ProjectFile> {
        val project = getProject(id) ?: return emptyMap()
        return fileIds.associateWith { fileId -> requireNotNull(project.files[fileId]) }
    }

    fun addFile(id: ProjectId, fileId: FileId, projectFile: ProjectFile) {
        val root = getProjectRoot()
        val project = root.projects[id] ?: throw IllegalArgumentException("Project not found")
        val files = project.files.toMutableMap()
        files[fileId] = projectFile

        val rootMutable = root.projects.toMutableMap()
        rootMutable[id] = project.copy(files = files)
        saveProjectRoot(root.copy(projects = rootMutable.toMap()))
    }

    fun getProjects(): Map<ProjectId, Project> {
        return getProjectRoot().projects
    }

    fun add(project: Project) {
        val root = getProjectRoot()
        val projects = root.projects.toMutableMap()
        if (projects.containsKey(project.id)) {
            throw IllegalArgumentException("Project with id ${project.id} already exists")
        }
        projects[project.id] = project
        saveProjectRoot(root.copy(projects = projects.toMap()))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Synchronized
    private fun getProjectRoot(): ProjectsRoot {
        return json.decodeFromStream<ProjectsRoot>(File(FILE).inputStream())
    }

    @Synchronized
    private fun saveProjectRoot(projectsRoot: ProjectsRoot) {
        File(FILE).writeText(json.encodeToString(projectsRoot))
    }
}


@Serializable
data class ProjectsRoot(
    @SerialName("projects")
    val projects: Map<ProjectId, Project>,
)

@Serializable
data class Project(
    @SerialName("id")
    val id: ProjectId,
    @SerialName("name")
    val name: String,
    @SerialName("files")
    val files: Map<FileId, ProjectFile>
)

@Serializable
data class ProjectFile(
    @SerialName("name")
    val name: String,
    @SerialName("dxf_file")
    val dxfFile: String,
    @SerialName("svg_file")
    val svgFile: String,
)

@JvmInline
@Serializable
value class ProjectId(val value: String)

@JvmInline
@Serializable
value class FileId(val value: String)
