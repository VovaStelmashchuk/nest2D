package com.nestapp.project

import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.minio.ProjectRepository
import com.nestapp.nest.ClosePolygon
import com.nestapp.nest.PolygonGenerator
import com.nestapp.nest.getPointsFromPath
import java.util.Locale

class ProjectMaker(
    private val projectRepository: ProjectRepository,
    private val svgWriter: SvgWriter,
    private val polygonGenerator: PolygonGenerator,
) {

    fun makeProject(
        projectName: String,
        previewFile: ByteArray?,
        previewFileNameExtension: String,
        dxfFileBytes: List<Pair<String, ByteArray>>
    ): String {
        val slug = createProjectSlug(projectName)

        previewFile?.let {
            projectRepository.uploadFileToMinioByteArray(
                it,
                "image/png",
                "projects/$slug/media/preview.$previewFileNameExtension"
            )
        }

        dxfFileBytes.forEach { (fileName, fileStream) ->
            projectRepository.uploadFileToMinioByteArray(
                fileStream,
                "application/dxf",
                "projects/$slug/files/$fileName"
            )
        }

        dxfFileBytes.forEach { (fileName, fileBytes) ->
            val dxfReader = DXFReader()
            dxfReader.parseFile(fileBytes.inputStream())
            val polygons = polygonGenerator.getPolygons(dxfReader.entities)

            val fileNameWithoutExtension = fileName.substringBeforeLast(".")
            val svgString = svgWriter.buildSvgString(polygons)
            projectRepository.uploadFileToMinioByteArray(
                bytes = svgString.toByteArray(),
                contentType = "image/svg+xml",
                objectName = "projects/$slug/files/$fileNameWithoutExtension.svg"
            )
        }

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
}
