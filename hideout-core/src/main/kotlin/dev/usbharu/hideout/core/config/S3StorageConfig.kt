package dev.usbharu.hideout.core.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@ConfigurationProperties("hideout.storage.s3")
@ConditionalOnProperty("hideout.storage.type", havingValue = "s3")
data class S3StorageConfig(
    val endpoint: String,
    val publicUrl: String,
    val bucket: String,
    val region: String,
    val accessKey: String,
    val secretKey: String
)

@Configuration
class AwsConfig {
    @Bean
    @ConditionalOnProperty("hideout.storage.type", havingValue = "s3")
    fun s3Client(awsConfig: S3StorageConfig): S3Client {
        return S3Client.builder()
            .endpointOverride(URI.create(awsConfig.endpoint))
            .region(Region.of(awsConfig.region))
            .credentialsProvider { AwsBasicCredentials.create(awsConfig.accessKey, awsConfig.secretKey) }
            .build()
    }
}
