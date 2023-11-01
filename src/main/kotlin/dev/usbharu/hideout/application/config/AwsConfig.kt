package dev.usbharu.hideout.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class AwsConfig {
    @Bean
    fun s3Client(awsConfig: StorageConfig): S3Client {
        return S3Client.builder()
            .endpointOverride(URI.create(awsConfig.endpoint))
            .region(Region.of(awsConfig.region))
            .credentialsProvider { AwsBasicCredentials.create(awsConfig.accessKey, awsConfig.secretKey) }
            .build()
    }
}
