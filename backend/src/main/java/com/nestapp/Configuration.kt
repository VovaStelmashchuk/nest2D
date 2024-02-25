package com.nestapp

import java.io.File

class Configuration(
    val baseUrl: String,
    val projectsFolder: File,
    val nestedFolder: File,
    val appVersion: String,
)
