package com.nestapp

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import org.jetbrains.exposed.sql.Database
import java.io.File

internal object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(CIO, environment = applicationEngineEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            developmentMode = true

            module {
                val appVersion = config.property("ktor.app.version").getString()

                val databaseUrl = config.property("ktor.database.url").getString()
                val databaseName = config.property("ktor.database.name").getString()
                val user = config.property("ktor.database.user").getString()
                val password = config.propertyOrNull("ktor.database.password")?.getString().orEmpty()

                Database.connect(
                    url = "jdbc:postgresql://$databaseUrl/$databaseName",
                    user = user,
                    password = password,
                )

                val configuration = Configuration(
                    baseUrl = config.property("ktor.app.base_url").getString(),
                    projectsFolder = File("mount/projects"),
                    nestedFolder = File("mount/nested"),
                    appVersion = appVersion,
                )

                val appComponent = AppComponent(
                    configuration,
                    this.log,
                )

                println("Starting server on ${configuration.baseUrl}")

                restConfig(appComponent)
            }

            val port = config.property("ktor.connector.port").getString().toInt()
            val host = config.property("ktor.connector.host").getString()

            connector {
                this.port = port
                this.host = host
            }
        }).start(wait = true)
    }
}
