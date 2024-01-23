package com.nestapp.projects

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

class ProjectsRepository {

    companion object {
        private const val FILE = "mount/app_data/projects.json"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Synchronized
    fun getProject(id: ProjectId): Project? {
        val root = Json.decodeFromStream<ProjectsRoot>(File(FILE).inputStream())
        return root.projects[id]
    }

    fun getFiles(id: ProjectId, fileIds: List<FileId>): Map<FileId, ProjectFile> {
        val project = getProject(id) ?: return emptyMap()
        return fileIds.associateWith { fileId -> requireNotNull(project.files[fileId]) }
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
