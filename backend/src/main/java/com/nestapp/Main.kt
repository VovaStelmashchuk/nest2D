package com.nestapp

import com.nestapp.files.dxf.DxfWriter
import com.nestapp.files.dxf.reader.DXFReader
import com.nestapp.files.svg.SvgWriter
import com.nestapp.nest.JaguarRequest
import com.nestapp.nest.PolygonGenerator
import com.typesafe.config.ConfigFactory
import io.ktor.server.cio.CIO
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import java.io.File
import java.io.IOException

internal object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        //val dxfFile = File("mount/projects/laser-cut-box/files/1x1.dxf")
        val dxfFile = File("mount/projects/from-prod-2/files/from-prod-1.dxf")

        val dxfReader = DXFReader()

        try {
            dxfReader.parseFile(dxfFile)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        val polygonGenerator = PolygonGenerator()

        val res = polygonGenerator.getPolygons(dxfReader.entities)

        SvgWriter().writePlacement(res, File("mount/test.svg"))
        DxfWriter().writeFile(res, File("mount/test.dxf"))

        JaguarRequest().buildJson(res)

        embeddedServer(CIO, environment = applicationEngineEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load())
            developmentMode = true

            module {
                val appVersion = config.property("ktor.app.version").getString()

                val configuration = Configuration(
                    baseUrl = config.property("ktor.app.base_url").getString(),
                    projectsFolder = File("mount/projects"),
                    appVersion = appVersion,
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
