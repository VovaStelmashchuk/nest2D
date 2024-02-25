package com.nestapp

import com.nestapp.files.PreviewGenerator
import com.nestapp.nest.Nest
import com.nestapp.nest.nfp.NfpCacheReader
import com.nestapp.nest.nfp.NfpCacheRepository
import com.nestapp.nest_api.NestApi
import com.nestapp.nest_api.NestedRepository
import com.nestapp.project.ProjectsRepository
import com.nestapp.project.files.ProjectFilesRepository
import com.nestapp.project.parts.PartsRepository
import kotlinx.serialization.json.Json
import org.slf4j.Logger

class AppComponent(
    val configuration: Configuration,
    logger: Logger,
) {

    val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    val partsRepository: PartsRepository = PartsRepository(
        json = json
    )

    val previewGenerator: PreviewGenerator = PreviewGenerator(
        partsRepository = partsRepository
    )

    val projectFilesRepository: ProjectFilesRepository = ProjectFilesRepository(
        configuration = configuration
    )

    val projectsRepository = ProjectsRepository()

    val nestedRepository = NestedRepository(
        partsRepository = partsRepository,
        configuration = configuration,
        json = json,
    )

    val nestApi = NestApi(
        nest = Nest(
            logger = logger,
            nfpCache = NfpCacheRepository(
                logger = logger,
                json = json
            ),
            nfpCacheReaderGetter = {
                NfpCacheReader(
                    json = json
                )
            }
        ),
        logger = logger,
    )
}
