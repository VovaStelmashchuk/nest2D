package com.nestapp

import com.nestapp.nest.Nest
import com.nestapp.nest.nfp.NfpCacheRepository
import com.nestapp.nest_api.NestApi
import com.nestapp.nest_api.NestedRepository
import com.nestapp.project.ProjectsRepository
import com.nestapp.project.files.ProjectFilesRepository
import kotlinx.serialization.json.Json
import org.slf4j.Logger

class AppComponent(
    val configuration: Configuration,
    logger: Logger,
) {

    val projectFilesRepository: ProjectFilesRepository = ProjectFilesRepository()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val projectsRepository = ProjectsRepository()
    val nestedRepository = NestedRepository(json)

    val nestApi = NestApi(
        nest = Nest(logger, NfpCacheRepository(logger)),
        logger = logger,
    )
}
