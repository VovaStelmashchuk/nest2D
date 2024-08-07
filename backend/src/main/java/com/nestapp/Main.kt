package com.nestapp

import com.typesafe.config.ConfigFactory
import io.ktor.server.cio.CIO
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer

internal object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(CIO, environment = applicationEngineEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            developmentMode = true

            module {
                val appVersion = config.property("ktor.app.version").getString()

                val configuration = Configuration(
                    baseUrl = config.property("ktor.app.base_url").getString(),
                    appVersion = appVersion,
                    endpoint = config.property("ktor.minio.endpoint").getString(),
                    port = config.property("ktor.minio.port").getString().toInt(),
                    accessKey = config.property("ktor.minio.access_key").getString(),
                    secretKey = config.property("ktor.minio.secret_key").getString(),
                )

                val appComponent = AppComponent(
                    configuration,
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
