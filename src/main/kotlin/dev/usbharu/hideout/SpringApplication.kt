package dev.usbharu.hideout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
class SpringApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<SpringApplication>(*args)
}
