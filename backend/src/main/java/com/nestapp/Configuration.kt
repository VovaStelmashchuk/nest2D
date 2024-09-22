package com.nestapp

class Configuration(
    val baseUrl: String,
    val appVersion: String,
    val mongoUrl: String,
    val jaguarUrl: String,
    val s3Config: S3Config,
) {
    data class S3Config(
        val endpoint: String,
        val accessKey: String,
        val secretKey: String,
        val region: String,
        val bucketName: String,
        val publicUrlStart: String,
    )
}
