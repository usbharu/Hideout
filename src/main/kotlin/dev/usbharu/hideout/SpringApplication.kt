package dev.usbharu.hideout

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringApplication

fun main(args: Array<String>) {
    runApplication<SpringApplication>(*args)
}
