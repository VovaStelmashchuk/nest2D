package com.nestapp.project.parts

import com.nestapp.files.dxf.DxfPath
import com.nestapp.files.dxf.EntityGrouper
import com.nestapp.files.dxf.GroupedEntity
import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.dxf.reader.Entity
import com.nestapp.files.dxf.reader.Line
import com.nestapp.files.dxf.reader.LwPolyline
import com.nestapp.project.files.ProjectFile
import com.nestapp.project.files.ProjectFilesTable
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.encodeToJsonElement
import org.apache.batik.ext.awt.geom.Polygon2D
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.io.File
import java.io.IOException

class PartsRepository(
    private val json: Json,
) {

    companion object {
        private const val CONNCETD_ENTITIES_TOLERANCE = 0.0001
    }

    init {
        transaction {
            SchemaUtils.create(PartsTable)
        }
    }

    fun addPartsFromFile(fileId: EntityID<Int>) {
        val file = transaction {
            ProjectFile.find { ProjectFilesTable.id eq fileId }.firstOrNull()
        } ?: throw IllegalArgumentException("File not found")

        val dxfFile = File(file.dxfFilePath)

        val dxfReader = DXFReader()

        try {
            dxfReader.parseFile(dxfFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        transaction {
            getEntities(dxfReader.entities)
                .forEach { (root, inside) ->
                    val rootJson = json.encodeToJsonElement(root.entities.map { mapEntity(it) })
                    val insideJson = json.encodeToJsonElement(inside.flatMap { it.entities }.map { mapEntity(it) })

                    DatabaseDxfPart.new {
                        this.fileId = fileId
                        this.root = rootJson.toString()
                        this.inside = insideJson.toString()
                    }
                }
        }
    }

    private fun mapEntity(entity: Entity): DataBaseDxfEntity {
        return when (entity) {
            is Line -> {
                DataBaseDxfEntity.Line(
                    start = DataBaseDxfEntity.Point(
                        x = entity.xStart,
                        y = entity.yStart,
                    ),
                    end = DataBaseDxfEntity.Point(
                        x = entity.xEnd,
                        y = entity.yEnd,
                    ),
                )
            }

            is LwPolyline -> {
                DataBaseDxfEntity.LwPolyline(
                    segments = entity.segments.map { segment ->
                        DataBaseDxfEntity.LwPolyline.Segment(
                            point = DataBaseDxfEntity.Point(
                                x = segment.dx,
                                y = segment.dy,
                            ),
                            bulge = segment.bulge,
                        )
                    }
                )
            }

            else -> throw IllegalStateException("Unknown entity type")
        }
    }

    private fun getEntities(
        entities: List<Entity>,
    ): Map<GroupedEntity, MutableList<GroupedEntity>> {
        val connectedEntities: List<GroupedEntity> = EntityGrouper.groupEntities(entities, CONNCETD_ENTITIES_TOLERANCE)
        val entitiesGroups: MutableMap<GroupedEntity, MutableList<GroupedEntity>> = mutableMapOf()

        for (parentIndex in connectedEntities.indices) {
            val parent: GroupedEntity = connectedEntities[parentIndex]
            for (childIndex in connectedEntities.indices) {
                if (parentIndex == childIndex) continue
                val child: GroupedEntity = connectedEntities[childIndex]

                if (isPathInsideAnother(parent.path, child.path)) {
                    entitiesGroups.getOrPut(parent) { mutableListOf() }.add(child)
                }
            }
        }

        val singleGroups: List<GroupedEntity> = connectedEntities
            .minus(entitiesGroups.keys)
            .minus(entitiesGroups.values.flatten().toSet())

        singleGroups.forEach {
            entitiesGroups[it] = mutableListOf()
        }

        return entitiesGroups.toMap()
    }

    private fun isPathInsideAnother(outerPath: Path2D.Double, innerPath: Path2D.Double): Boolean {
        val parentNestPath = toDxfPath(outerPath)
        val childNestPath = toDxfPath(innerPath)
        val parent = toPolygon2D(parentNestPath)
        val child = toPolygon2D(childNestPath)

        return parent.contains(child)
    }

    private fun toDxfPath(path: Path2D): DxfPath {
        val list = mutableListOf<Point2D.Double>()

        val at = AffineTransform()
        val iter = path.getPathIterator(at, CONNCETD_ENTITIES_TOLERANCE)
        val coords = DoubleArray(6)
        while (!iter.isDone) {
            val type = iter.currentSegment(coords)

            when (type) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    list.add(Point2D.Double(coords[0], coords[1]))
                }

                PathIterator.SEG_QUADTO -> {
                    list.add(Point2D.Double(coords[2], coords[3]))
                }

                PathIterator.SEG_CUBICTO -> {
                    list.add(Point2D.Double(coords[4], coords[5]))
                }
            }
            iter.next()
        }

        return DxfPath(list)
    }

    private fun toPolygon2D(nestPath: DxfPath): Polygon2D { ///TODO optimize
        val xp: MutableList<Float> = ArrayList()
        val yp: MutableList<Float> = ArrayList()
        for (s in nestPath.segments) {
            xp.add(s.getX().toFloat())
            yp.add(s.getY().toFloat())
        }

        val xparray = FloatArray(xp.size)
        val yparray = FloatArray(yp.size)
        var i = 0

        for (f in xp) {
            xparray[i++] = f
        }
        i = 0
        for (f in yp) {
            yparray[i++] = f
        }

        return Polygon2D(xparray, yparray, nestPath.segments.size)
    }

    private fun Polygon2D.contains(polygon2D: Polygon2D): Boolean {
        return (0 until polygon2D.npoints)
            .map {
                polygon2D.xpoints[it].toDouble() to polygon2D.ypoints[it].toDouble()
            }
            .all { (x, y) ->
                this.contains(x, y)
            }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class DataBaseDxfEntity {

    @Serializable
    @SerialName("line")
    data class Line(
        @SerialName("start")
        val start: Point,
        @SerialName("end")
        val end: Point,
    ) : DataBaseDxfEntity()

    @Serializable
    @SerialName("LwPolyline")
    data class LwPolyline(
        @SerialName("segments")
        val segments: List<Segment>,
    ) : DataBaseDxfEntity() {
        @Serializable
        data class Segment(
            @SerialName("point")
            val point: Point,
            @SerialName("bulge")
            val bulge: Double,
        )
    }

    @Serializable
    data class Point(
        val x: Double,
        val y: Double,
    )
}


object PartsTable : IntIdTable(name = "parts", columnName = "id") {
    val fileId = reference("file_id", ProjectFilesTable)
    val root = text("root")
    val inside = text("inside")
}

class DatabaseDxfPart(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DatabaseDxfPart>(PartsTable)

    var fileId by PartsTable.fileId
    var root by PartsTable.root
    var inside by PartsTable.inside
}
