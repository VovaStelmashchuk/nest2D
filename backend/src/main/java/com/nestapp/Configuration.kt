package com.nestapp

import java.io.File

class Configuration(
    val baseUrl: String,
    val projectsFolder: File,
    val appVersion: String,
)

const val TOLERANCE = 1e-2
