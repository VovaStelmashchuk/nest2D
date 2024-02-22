package com.nestapp.nest_api

import com.nestapp.files.DxfPartPlacement
import com.nestapp.files.dxf.DxfWriter
import com.nestapp.files.svg.SvgWriter
import com.nestapp.nest.data.Placement
import com.nestapp.project.ProjectSlug
import com.nestapp.project.parts.PartsRepository
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class NestedRepository(
    private val partsRepository: PartsRepository,
    private val json: Json,
) {

    init {
        transaction {
            SchemaUtils.create(NestedTable)
        }
    }

    fun getNested(id: Int): Nested? {
        return null
    }

    fun saveNestPlacement(
        placement: List<Placement>,
        nestInput: NestInput,
    ): Int {
        val nested = transaction {
            NestedDatbase.new {
                this.nestInput = json.encodeToString(NestInput.serializer(), nestInput)
            }
        }

        val nestedId = nested.id.value

        val folder = File(
            "mount/nested/",
            "${nestedId}_${nestInput.projectSlug.value}"
        )
        folder.mkdir()

        saveFiles(folder, placement, nestInput)

        return nestedId
    }

    private fun saveFiles(
        folder: File,
        placement: List<Placement>,
        nestInput: NestInput
    ) {
        val dxfFile = File(folder, "cad_file.dxf")
        val svgFile = File(folder, "preview.svg")

        val parts = partsRepository.getPartsByIds(placement.map { it.bid })
        val dxfPartPlacement = placement.map {
            val part = parts[it.bid] ?: throw Exception("Part not found")

            DxfPartPlacement(
                placement = it,
                part = part,
            )
        }

        val svgWriter = SvgWriter()
        svgWriter.writePlacement(
            dxfPartPlacement,
            svgFile,
            nestInput.plateWidth,
            nestInput.plateHeight,
        )

        val dxfWriter = DxfWriter()
        dxfWriter.writeFile(
            dxfPartPlacement,
            dxfFile,
        )
    }
}

object NestedTable : IntIdTable(name = "nested", columnName = "id") {
    val nestInput = text("nest_input")
}

class NestedDatbase(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NestedDatbase>(NestedTable)

    var nestInput by NestedTable.nestInput
}

@Serializable
data class Nested(
    @SerialName("id")
    val id: Int,
    @SerialName("dxf_file")
    val dxfFile: String,
    @SerialName("svg_file")
    val svgFile: String,
    @SerialName("project_id")
    val projectId: ProjectSlug,
    @SerialName("file_counts")
    val fileCounts: Map<String, Int>,
    @SerialName("plate_width")
    val plateWidth: Int,
    @SerialName("plate_height")
    val plateHeight: Int,
)
