package com.nestapp

import com.nestapp.files.svg.SvgWriter
import com.nestapp.nest.PolygonGenerator
import com.nestapp.nest.jaguar.JaguarRequest
import com.nestapp.nest.nestRestApi
import com.nestapp.project.ProjectMaker
import com.nestapp.project.projectsRestController
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

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
        install(HttpTimeout) {
            requestTimeoutMillis = TimeUnit.MINUTES.toMillis(3)
            connectTimeoutMillis = TimeUnit.MINUTES.toMillis(1)
            socketTimeoutMillis = TimeUnit.MINUTES.toMillis(3)
        }
    }
}

fun Application.restConfig(
    appComponent: AppComponent,
) {
    install(StatusPages) {
        exception<Throwable> { cause, throwable ->
            println(throwable.printStackTrace())
            cause.respond(HttpStatusCode.InternalServerError, "Error: $throwable")
        }
    }

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
        projectMaker = ProjectMaker(
            minioProjectRepository = appComponent.minioProjectRepository,
            projectRepository = appComponent.projectRepository,
            svgWriter = SvgWriter(),
            polygonGenerator = PolygonGenerator(),
        ),
        projectRepository = appComponent.projectRepository,
    )

    nestRestApi(
        jaguarRequest = JaguarRequest(client),
        polygonGenerator = PolygonGenerator(),
        minioProjectRepository = appComponent.minioProjectRepository,
        nestHistoryRepository = appComponent.nestHistoryRepository,
        configuration = appComponent.configuration,
        minioFileUpload = appComponent.minioFileUpload,
    )

    get("/version") {
        call.respondText(appComponent.configuration.appVersion)
    }
}
