package com.nestapp

import com.nestapp.nest_api.UserInputExecution
import com.nestapp.nest_api.nestRestApi
import com.nestapp.project.files.filesRestController
import com.nestapp.project.projectsRestController
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

fun Application.restConfig(appComponent: AppComponent) {
    install(StatusPages) {
        exception<UserInputExecution> { call, userInputExecution ->
            println(userInputExecution.printStackTrace())
            call.respond(HttpStatusCode.BadRequest, userInputExecution.getBody())
        }
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

    routing {
        route("/api") {
            setupRouter(appComponent)
        }
    }
}

fun Route.setupRouter(appComponent: AppComponent) {
    projectsRestController(
        appComponent.configuration,
        appComponent.projectsRepository
    )

    nestRestApi(
        configuration = appComponent.configuration,
        projectsRepository = appComponent.projectsRepository,
        nestedRepository = appComponent.nestedRepository,
        nestApi = appComponent.nestApi,
    )

    filesRestController(
        configuration = appComponent.configuration,
        projectsRepository = appComponent.projectsRepository,
        projectFilesRepository = appComponent.projectFilesRepository,
    )

    get("/version") {
        call.respondText(appComponent.configuration.appVersion)
    }
}
