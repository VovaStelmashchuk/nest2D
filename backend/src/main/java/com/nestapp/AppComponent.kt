package com.nestapp

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.nestapp.s3.S3FileUpload
import com.nestapp.s3.S3ProjectRepository
import com.nestapp.mongo.NestHistoryRepository
import com.nestapp.mongo.ProjectRepository

class AppComponent(
    val configuration: Configuration,
) {

    private var awsCreds: BasicAWSCredentials = BasicAWSCredentials(configuration.s3Config.accessKey, configuration.s3Config.secretKey)

    private var s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
        .withCredentials(AWSStaticCredentialsProvider(awsCreds))
        .withEndpointConfiguration(
            AwsClientBuilder.EndpointConfiguration(configuration.s3Config.endpoint, configuration.s3Config.region)
        )
        .build()

    val s3FileUpload = S3FileUpload(s3Client)

    val s3ProjectRepository: S3ProjectRepository = S3ProjectRepository(s3Client, s3FileUpload)

    private val mongoClient = MongoClient.create(connectionString = configuration.mongoUrl)

    val nestHistoryRepository = NestHistoryRepository(mongoClient)

    val projectRepository = ProjectRepository(mongoClient)

}
