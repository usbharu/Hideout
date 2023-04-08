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
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubServiceImpl
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserServiceImpl
import dev.usbharu.hideout.service.impl.UserAuthService
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyServiceImpl
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val Application.property: Application.(propertyName: String) -> String
    get() = {
        environment.config.property(it).getString()
    }

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    Config.configData = ConfigData(
        url = property("hideout.url"),
        objectMapper = jacksonObjectMapper().enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    )

    val module = org.koin.dsl.module {
        single<Database> {
            Database.connect(
                url = property("hideout.database.url"),
                driver = property("hideout.database.driver"),
                user = property("hideout.database.username"),
                password = property("hideout.database.password")
            )
        }

        single<IUserRepository> { UserRepository(get()) }
        single<IUserAuthRepository> { UserAuthRepository(get()) }
        single<IUserAuthService> { UserAuthService(get(), get()) }
        single<HttpSignatureVerifyService> { HttpSignatureVerifyServiceImpl(get()) }
        single<ActivityPubService> { ActivityPubServiceImpl() }
        single<UserService> { UserService(get()) }
        single<ActivityPubUserService> { ActivityPubUserServiceImpl(get(), get()) }
    }

    configureKoin(module)
    configureHTTP()
    configureSockets()
    configureMonitoring()
    configureSerialization()
    configureRouting(
        inject<HttpSignatureVerifyService>().value,
        inject<ActivityPubService>().value,
        inject<UserService>().value,
        inject<ActivityPubUserService>().value
    )
}
