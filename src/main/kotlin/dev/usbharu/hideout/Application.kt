package dev.usbharu.hideout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.plugins.*
import dev.usbharu.hideout.repository.IUserAuthRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.repository.UserAuthRepository
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.routing.*
import dev.usbharu.hideout.service.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val module = org.koin.dsl.module {

        single<Database> {
            Database.connect(
                url = environment.config.property("hideout.database.url").getString(),
                driver = environment.config.property("hideout.database.driver").getString(),
            )
        }
        single<ConfigData> {
            ConfigData(
                url = environment.config.propertyOrNull("hideout.url")?.getString()
                    ?: environment.config.property("hideout.hostname").getString(),
                objectMapper =  jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                    .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            )
        }
        single<HttpClient> { HttpClient(CIO) }
        single<IUserRepository> { UserRepository(get()) }
        single<IUserAuthRepository> { UserAuthRepository(get()) }
        single<IUserAuthService> { UserAuthService(get(), get()) }
        single<UserService> { UserService(get()) }
        single<ActivityPubService> { ActivityPubService() }
        single<ActivityPubUserService> { ActivityPubUserService(get(), get(),get(),get()) }
        single<IWebFingerService> { WebFingerService(get(),get()) }
    }
    configureKoin(module)
    val configData by inject<ConfigData>()
    Config.configData = configData
    val decode = Base64.getDecoder().decode("76pc9N9hspQqapj30kCaLJA14O/50ptCg50zCA1oxjA=")

    val pair = "admin" to decode
    println(pair)
    val userAuthService by inject<IUserAuthService>()
    val userService by inject<UserService>()
    configureSecurity(userAuthService)
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    val activityPubUserService by inject<ActivityPubUserService>()
    user(userService, activityPubUserService)
    login()
    register(userAuthService)
    wellKnown(userService)
    val activityPubService by inject<ActivityPubService>()
    userActivityPubRouting(activityPubService, activityPubUserService)
}
