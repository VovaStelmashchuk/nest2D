package com.nestapp.project.parts

import com.nestapp.converts.makeListOfPoints
import com.nestapp.files.dxf.EntityGrouper
import com.nestapp.files.dxf.GroupedEntity
import com.nestapp.files.dxf.common.LSegment
import com.nestapp.files.dxf.common.RealPoint
import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.dxf.reader.Entity
import com.nestapp.files.dxf.reader.Line
import com.nestapp.files.dxf.reader.LwPolyline
import com.nestapp.files.dxf.writter.parts.DXFEntity
import com.nestapp.files.dxf.writter.parts.DXFLWPolyline
import com.nestapp.files.dxf.writter.parts.DXFLine
import com.nestapp.nest.data.Placement
import com.nestapp.project.Project
import com.nestapp.project.ProjectsTable
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
import java.awt.geom.Arc2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.io.File
import java.io.IOException
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PartsRepository(
    private val json: Json,
) {

    companion object {
        private const val CONNECT_ENTITIES_TOLERANCE = 0.0001
    }

    init {
        transaction {
            SchemaUtils.create(PartsTable)
        }
    }

    fun getPartByFileId(fileId: Int): List<DxfPart> = transaction {
        DatabaseDxfPart.find { PartsTable.fileId eq fileId }.toList()
            .map {
                DxfPart(
                    id = it.id.toString(),
                    root = json.decodeFromString(it.root),
                    inside = json.decodeFromString(it.inside),
                )
            }
    }

    fun getPartsByIds(ids: List<String>): Map<String, DxfPart> {
        return transaction {
            DatabaseDxfPart.find { PartsTable.id inList ids.map { it.toInt() } }
                .associate {
                    val stringId = it.id.toString()
                    stringId to DxfPart(
                        id = stringId,
                        root = json.decodeFromString(it.root),
                        inside = json.decodeFromString(it.inside),
                    )
                }
        }
    }

    fun getParts(projectSlug: String, fileName: String): List<DatabaseDxfPart> = transaction {
        val project = Project.find { ProjectsTable.slug eq projectSlug }.firstOrNull()
            ?: throw IllegalArgumentException("Project not found")

        val file = project.files.find { it.fileName == fileName }
            ?: throw IllegalArgumentException("File not found")

        DatabaseDxfPart.find { PartsTable.fileId eq file.id }.toList()
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
        val connectedEntities: List<GroupedEntity> = EntityGrouper.groupEntities(entities, CONNECT_ENTITIES_TOLERANCE)
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
        val parentNestPath = makeListOfPoints(outerPath, CONNECT_ENTITIES_TOLERANCE)
        val childNestPath = makeListOfPoints(innerPath, CONNECT_ENTITIES_TOLERANCE)
        val parent = toPolygon2D(parentNestPath)
        val child = toPolygon2D(childNestPath)

        return parent.contains(child)
    }

    private fun toPolygon2D(pointList: List<Point2D.Double>): Polygon2D { ///TODO optimize
        val xp: MutableList<Float> = ArrayList()
        val yp: MutableList<Float> = ArrayList()
        for (s in pointList) {
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

        return Polygon2D(xparray, yparray, pointList.size)
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

data class DxfPart(
    val id: String,
    val root: List<DataBaseDxfEntity>,
    val inside: List<DataBaseDxfEntity>
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class DataBaseDxfEntity {

    abstract fun toPath2D(): Path2D.Double

    abstract fun toWritableEntity(placement: Placement): DXFEntity

    @Serializable
    @SerialName("line")
    data class Line(
        @SerialName("start")
        val start: Point,
        @SerialName("end")
        val end: Point,
    ) : DataBaseDxfEntity() {
        override fun toPath2D(): Path2D.Double {
            val path = Path2D.Double()
            path.moveTo(start.x, start.y)
            path.lineTo(end.x, end.y)
            return path
        }

        override fun toWritableEntity(placement: Placement): DXFEntity {
            val start = RealPoint(start.x, start.y)
            val end = RealPoint(end.x, end.y)
            return DXFLine(start.transform(placement), end.transform(placement))
        }
    }

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
        ) {
            fun transform(placement: Placement): Segment {
                return Segment(point.transform(placement), bulge)
            }
        }

        override fun toPath2D(): Path2D.Double {
            val path = Path2D.Double()
            var first = true
            var lastX = 0.0
            var lastY = 0.0
            var firstX = 0.0
            var firstY = 0.0
            var bulge = 0.0
            for (segment in segments) {
                if (bulge != 0.0) {
                    path.append(
                        getArcBulge(
                            lastX,
                            lastY,
                            segment.point.x.also { lastX = it },
                            segment.point.y.also { lastY = it },
                            bulge
                        ),
                        true
                    )
                } else {
                    if (first) {
                        path.moveTo(
                            segment.point.x.also { lastX = it }.also { firstX = it },
                            segment.point.y.also { lastY = it }.also { firstY = it }
                        )
                        first = false
                    } else {
                        path.lineTo(segment.point.x.also { lastX = it }, segment.point.y.also { lastY = it })
                    }
                }
                bulge = segment.bulge
            }

            if (bulge != 0.0) {
                path.append(getArcBulge(lastX, lastY, firstX, firstY, bulge), true)
            } else {
                path.lineTo(firstX, firstY)
            }
            return path
        }

        override fun toWritableEntity(placement: Placement): DXFEntity {
            return DXFLWPolyline(
                segments = segments.map {
                    val segment = it.transform(placement)
                    LSegment(segment.point.x, segment.point.y, segment.bulge)
                },
                closed = true
            )
        }

        /**
         * See: http://darrenirvine.blogspot.com/2015/08/polylines-radius-bulge-turnaround.html
         *
         * @param sx    Starting x for Arc
         * @param sy    Starting y for Arc
         * @param ex    Ending x for Arc
         * @param ey    Ending y for Arc
         * @param bulge bulge factor (bulge > 0 = clockwise, else counterclockwise)
         * @return Arc2D.Double object
         */
        private fun getArcBulge(sx: Double, sy: Double, ex: Double, ey: Double, bulge: Double): Arc2D.Double {
            val p1 = Point2D.Double(sx, sy)
            val p2 = Point2D.Double(ex, ey)
            val mp = Point2D.Double((p2.x + p1.x) / 2, (p2.y + p1.y) / 2)
            val bp = Point2D.Double(mp.x - (p1.y - mp.y) * bulge, mp.y + (p1.x - mp.x) * bulge)
            val u = p1.distance(p2)
            val b = (2 * mp.distance(bp)) / u
            val radius = u * ((1 + b * b) / (4 * b))
            val dx = mp.x - bp.x
            val dy = mp.y - bp.y
            val mag = sqrt(dx * dx + dy * dy)
            val cp = Point2D.Double(bp.x + radius * (dx / mag), bp.y + radius * (dy / mag))
            val startAngle = 180 - Math.toDegrees(atan2(cp.y - p1.y, cp.x - p1.x))
            val opp = u / 2
            val extent = Math.toDegrees(asin(opp / radius)) * 2
            val extentAngle = if (bulge >= 0) -extent else extent
            val ul = Point2D.Double(cp.x - radius, cp.y - radius)
            return Arc2D.Double(ul.x, ul.y, radius * 2, radius * 2, startAngle, extentAngle, Arc2D.OPEN)
        }
    }

    @Serializable
    data class Point(
        val x: Double,
        val y: Double,
    ) {
        fun transform(placement: Placement): Point {
            val angle: Double = placement.rotate * Math.PI / 180
            val translateX = placement.translate.x
            val translateY = placement.translate.y

            val rotatedX = x * cos(angle) - y * sin(angle)
            val rotatedY = y * cos(angle) + x * sin(angle)

            val translatedX = rotatedX + translateX
            val translatedY = rotatedY + translateY

            return Point(translatedX, translatedY)
        }
    }
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
