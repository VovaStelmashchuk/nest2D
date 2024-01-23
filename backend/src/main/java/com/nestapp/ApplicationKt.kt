package com.nestapp

import com.nestapp.nest_api.nestRestApi
import com.nestapp.projects.ProjectsRepository
import com.nestapp.projects.projectRest
import io.ktor.http.HttpMethod.Companion.DefaultMethods
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

fun main() {
    val projectsRepository = ProjectsRepository()
    embeddedServer(Netty, port = 8080) {
        install(StatusPages)
        install(AutoHeadResponse)

        install(CORS) {
            anyHost()
            allowHeaders { true }
            allowCredentials = true
            DefaultMethods.forEach(::allowMethod)

            allowNonSimpleContentTypes = true
        }

        install(ContentNegotiation) {
            json()
        }

        routing {
            projectRest(File("mount"), projectsRepository)
            nestRestApi(projectsRepository)

            get("/") {
                call.respondText("Hello, world!")
            }
        }
    }.start(wait = true)
}
