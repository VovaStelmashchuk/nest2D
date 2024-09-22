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

                val s3Config = Configuration.S3Config(
                    endpoint = config.property("ktor.s3.endpoint").getString(),
                    accessKey = config.property("ktor.s3.access_key").getString(),
                    secretKey = config.property("ktor.s3.secret_key").getString(),
                    region = config.property("ktor.s3.region").getString(),
                    bucketName = config.property("ktor.s3.bucket").getString(),
                    publicUrlStart = config.property("ktor.s3.public_url").getString()
                )

                val configuration = Configuration(
                    baseUrl = config.property("ktor.app.base_url").getString(),
                    appVersion = appVersion,
                    mongoUrl = config.property("ktor.mongo.url").getString(),
                    jaguarUrl = config.property("ktor.jaguar.url").getString(),
                    s3Config = s3Config,
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
