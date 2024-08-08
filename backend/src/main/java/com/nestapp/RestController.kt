package com.nestapp

import com.nestapp.files.svg.SvgWriter
import com.nestapp.minio.MinioFileUpload
import com.nestapp.nest.PolygonGenerator
import com.nestapp.nest.jaguar.JaguarRequest
import com.nestapp.nest.nestRestApi
import com.nestapp.project.ProjectMaker
import com.nestapp.project.projectsRestController
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun createHttpClient(): HttpClient {
    return HttpClient {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
    }
}

fun Application.restConfig(
    appComponent: AppComponent,
) {
    install(AutoHeadResponse)

    install(CORS) {
        anyHost()
        allowHeaders { true }
        allowCredentials = true
        HttpMethod.DefaultMethods.forEach(::allowMethod)

        allowNonSimpleContentTypes = true
    }

    install(ContentNegotiation) {
        json()
    }

    val client = createHttpClient()

    routing {
        route("/api") {
            setupRouter(appComponent, client)
        }
    }
}

fun Route.setupRouter(
    appComponent: AppComponent,
    client: HttpClient,
) {
    projectsRestController(
        configuration = appComponent.configuration,
        projectRepository = appComponent.projectRepository,
        projectMaker = ProjectMaker(
            projectRepository = appComponent.projectRepository,
            svgWriter = SvgWriter(),
            polygonGenerator = PolygonGenerator(),
        ),
    )

    nestRestApi(
        jaguarRequest = JaguarRequest(client),
        polygonGenerator = PolygonGenerator(),
        projectRepository = appComponent.projectRepository,
        nestHistoryRepository = appComponent.nestHistoryRepository,
        configuration = appComponent.configuration,
        minioFileUpload = appComponent.minioFileUpload,
    )

    get("/version") {
        call.respondText(appComponent.configuration.appVersion)
    }
}
