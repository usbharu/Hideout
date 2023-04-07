package dev.usbharu.hideout

import dev.usbharu.hideout.plugins.*
import dev.usbharu.hideout.repository.IUserAuthRepository
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.repository.UserAuthRepository
import dev.usbharu.hideout.repository.UserRepository
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubServiceImpl
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
    }

    configureKoin(module)
    configureHTTP()
    configureSockets()
    configureMonitoring()
    configureSerialization()
    configureRouting(
        inject<HttpSignatureVerifyService>().value,
        inject<ActivityPubService>().value,
        inject<UserService>().value
    )
}
