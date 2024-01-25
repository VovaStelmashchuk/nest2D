package com.nestapp.nest_api

import com.google.gson.annotations.SerializedName
import com.nestapp.projects.FileId
import com.nestapp.projects.ProjectId
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

class NestedRepository(
    private val json: Json,
) {

    companion object {
        private const val FILE = "mount/app_data/nested.json"
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Synchronized
    private fun getNested(): NestedList {
        return json.decodeFromStream<NestedList>(File(FILE).inputStream())
    }

    @Synchronized
    private fun saveNested(nestedList: NestedList) {
        File(FILE).writeText(json.encodeToString(nestedList))
    }

    fun getNextId(): Int {
        return (getNested().nested.maxOfOrNull { it.id } ?: 0) + 1
    }

    fun addNested(nestedOutput: Nested): Int {
        val nested = getNested().nested.toMutableList()
        nested.add(nestedOutput)
        saveNested(NestedList(nested))
        return nestedOutput.id
    }

    fun getNested(id: Int): Nested? {
        return getNested().nested.firstOrNull { it.id == id }
    }
}

@Serializable
data class NestedList(
    @SerializedName("nested")
    val nested: List<Nested>
)

@Serializable
data class Nested(
    @SerialName("id")
    val id: Int,
    @SerialName("dxf_file")
    val dxfFile: String,
    @SerialName("svg_file")
    val svgFile: String,
    @SerialName("project_id")
    val projectId: ProjectId,
    @SerialName("file_counts")
    val fileCounts: Map<FileId, Int>,
    @SerialName("plate_width")
    val plateWidth: Int,
    @SerialName("plate_height")
    val plateHeight: Int,
)
