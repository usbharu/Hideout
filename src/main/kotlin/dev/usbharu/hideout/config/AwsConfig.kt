package dev.usbharu.hideout.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class AwsConfig {
    @Bean
    fun s3(awsConfig: StorageConfig): S3Client {
        return S3Client.builder()
            .credentialsProvider { AwsBasicCredentials.create(awsConfig.accessKey, awsConfig.secretKey) }
            .build()
    }
}
