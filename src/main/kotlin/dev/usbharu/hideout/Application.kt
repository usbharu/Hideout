package dev.usbharu.hideout

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.plugins.*
import dev.usbharu.hideout.repository.IUserAuthRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.repository.UserAuthRepository
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.routing.login
import dev.usbharu.hideout.routing.register
import dev.usbharu.hideout.routing.user
import dev.usbharu.hideout.routing.wellKnown
import dev.usbharu.hideout.service.ActivityPubUserService
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.UserAuthService
import dev.usbharu.hideout.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject
import java.util.Base64

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val module = org.koin.dsl.module {

        single<Database>{ Database.connect(
            url = "jdbc:h2:./test;MODE=POSTGRESQL",
            driver = "org.h2.Driver",
        ) }
        single<ConfigData>{ ConfigData(
            environment.config.property("hideout.hostname").getString(),
            jacksonObjectMapper()
        ) }
        single<IUserRepository>{UserRepository(get())}
        single<IUserAuthRepository>{UserAuthRepository(get())}
        single<IUserAuthService>{ UserAuthService(get(),get()) }
        single<UserService> { UserService(get()) }
        single<ActivityPubUserService> { ActivityPubUserService(get())}
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
    configureRouting()
    val activityPubUserService by inject<ActivityPubUserService>()
    user(userService,activityPubUserService)
    login()
    register(userAuthService)
    wellKnown(userService)
}
