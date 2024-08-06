package com.nestapp

import java.io.File

class Configuration(
    val baseUrl: String,
    val projectsFolder: File,
    val appVersion: String,
    val endpoint: String,
    val port: Int,
    val accessKey: String,
    val secretKey: String
)

const val TOLERANCE = 1e-2
