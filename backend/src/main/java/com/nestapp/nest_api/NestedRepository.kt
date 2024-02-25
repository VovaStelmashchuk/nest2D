package com.nestapp.nest_api

import com.nestapp.Configuration
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
    private val configuration: Configuration,
    private val partsRepository: PartsRepository,
    private val json: Json,
) {

    init {
        transaction {
            SchemaUtils.create(NestedTable)
        }
    }

    fun getNested(id: Int): NestedDatabase? {
        return transaction {
            NestedDatabase.findById(id)
        }
    }

    fun saveNestPlacement(
        placement: List<Placement>,
        nestInput: NestInput,
    ): Int {
        val nested = transaction {
            NestedDatabase.new {
                this.nestInput = json.encodeToString(NestInput.serializer(), nestInput)
            }
        }

        val nestedId = nested.id.value

        val folder = File(
            configuration.nestedFolder,
            "${nestedId}_${nestInput.projectSlug.value}"
        )
        folder.mkdirs()
        val svgFile = File(folder, "preview.svg")
        val dxfFile = File(folder, "cad_file.dxf")

        saveFiles(svgFile, dxfFile, placement, nestInput)

        transaction {
            nested.svgFile = svgFile.absolutePath
            nested.dxfFile = dxfFile.absolutePath
        }

        return nestedId
    }

    private fun saveFiles(
        svgFile: File,
        dxfFile: File,
        placement: List<Placement>,
        nestInput: NestInput
    ) {
        val parts = partsRepository.getPartsByIds(placement.map { it.bid.substringBefore("+") })
        val dxfPartPlacement = placement.map {
            val part = parts[it.bid.substringBefore("+")] ?: throw Exception("Part not found ${it.bid}")

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
    val svgFile = text("svg_file").default("")
    val dxfFile = text("dxf_file").default("")
}

class NestedDatabase(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<NestedDatabase>(NestedTable)

    var nestInput by NestedTable.nestInput
    var svgFile by NestedTable.svgFile
    var dxfFile by NestedTable.dxfFile
}
