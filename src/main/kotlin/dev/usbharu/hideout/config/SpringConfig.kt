package dev.usbharu.hideout.config

import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class SpringConfig {

    @Autowired
    lateinit var dbConfig: DatabaseConnectConfig

    @Autowired
    lateinit var config: ApplicationConfig

    @Bean
    fun database(): Database {
        return Database.connect(
            url = dbConfig.url,
            driver = dbConfig.driver,
            user = dbConfig.user,
            password = dbConfig.password
        )
    }
}

@ConfigurationProperties("hideout")
data class ApplicationConfig(
    val url: URL
)

@ConfigurationProperties("hideout.database")
data class DatabaseConnectConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String
)
