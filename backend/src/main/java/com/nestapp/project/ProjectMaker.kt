package com.nestapp.project

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.minio.MinioProjectRepository
import com.nestapp.mongo.ProjectDatabase
import com.nestapp.mongo.ProjectRepository
import com.nestapp.nest.PolygonGenerator
import org.bson.types.ObjectId
import java.util.Locale

class ProjectMaker(
    private val minioProjectRepository: MinioProjectRepository,
    private val projectRepository: ProjectRepository,
    private val svgWriter: SvgWriter,
    private val polygonGenerator: PolygonGenerator,
) {

    suspend fun makeProject(
        projectName: String,
        previewFile: ByteArray?,
        previewFileNameExtension: String,
        dxfFileBytes: List<Pair<String, ByteArray>>
    ): String {
        val slug = createProjectSlug(projectName)

        previewFile?.let {
            minioProjectRepository.uploadFileToMinioByteArray(
                it,
                "image/png",
                "projects/$slug/media/preview.$previewFileNameExtension"
            )
        }

        dxfFileBytes.forEach { (fileName, fileStream) ->
            minioProjectRepository.uploadFileToMinioByteArray(
                fileStream,
                "application/dxf",
                "projects/$slug/files/$fileName"
            )
        }

        dxfFileBytes.forEach { (fileName, fileBytes) ->
            val dxfReader = DXFReader()
            dxfReader.parseFile(fileBytes.inputStream())
            val polygons = polygonGenerator.getMergedAndCombinedPolygons(dxfReader.entities, SVG_TOLERANCE)

            val fileNameWithoutExtension = fileName.substringBeforeLast(".")
            val svgString = svgWriter.buildSvgString(polygons)
            minioProjectRepository.uploadFileToMinioByteArray(
                bytes = svgString.toByteArray(),
                contentType = "image/svg+xml",
                objectName = "projects/$slug/files/$fileNameWithoutExtension.svg"
            )
        }

        projectRepository.insertProject(
            ProjectDatabase(
                id = ObjectId(),
                name = projectName,
                projectSlug = slug,
                preview = previewFile?.let { "files/projects/$slug/media/preview.png" },
                files = dxfFileBytes.map { (fileName, _) -> fileName.substringBeforeLast(".") },
            )
        )

        return slug
    }

    private fun createProjectSlug(inputString: String): String {
        if (inputString.isBlank()) {
            throw IllegalArgumentException("Project name cannot be blank")
        }
        val filteredString = inputString.filter { it.isLetter() || it.isWhitespace() || it.isDigit() }
        val slug = filteredString.replace(" ", "-").lowercase(Locale.getDefault())
        return slug
    }

    companion object {
        private const val SVG_TOLERANCE = 0.1
    }
}
