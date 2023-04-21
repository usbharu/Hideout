package dev.usbharu.hideout

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.plugins.*
import dev.usbharu.hideout.repository.*
import dev.usbharu.hideout.routing.register
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.IUserAuthService
import dev.usbharu.hideout.service.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.service.activitypub.*
import dev.usbharu.hideout.service.impl.PostService
import dev.usbharu.hideout.service.impl.UserAuthService
import dev.usbharu.hideout.service.impl.UserService
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.job.KJobJobQueueParentService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyServiceImpl
import dev.usbharu.kjob.exposed.ExposedKJob
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.server.application.*
import kjob.core.kjob
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.ext.inject

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val Application.property: Application.(propertyName: String) -> String
    get() = {
        environment.config.property(it).getString()
    }

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.parent() {

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
        single<JobQueueParentService> {
            val kJobJobQueueService = KJobJobQueueParentService(get())
            kJobJobQueueService.init(listOf())
            kJobJobQueueService
        }
        single<HttpClient> {
            HttpClient(CIO).config {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
                install(httpSignaturePlugin) {
                    keyMap = KtorKeyMap(get())
                }
            }
        }
        single<ActivityPubFollowService> { ActivityPubFollowServiceImpl(get(), get(), get(), get()) }
        single<ActivityPubService> { ActivityPubServiceImpl(get(), get()) }
        single<UserService> { UserService(get()) }
        single<ActivityPubUserService> { ActivityPubUserServiceImpl(get(), get(), get()) }
        single<ActivityPubNoteService> { ActivityPubNoteServiceImpl(get(), get(), get()) }
        single<IPostService> { PostService(get(), get()) }
        single<IPostRepository> { PostRepositoryImpl(get(), TwitterSnowflakeIdGenerateService) }
    }


    configureKoin(module)
    configureHTTP()
    configureStaticRouting()
    configureMonitoring()
    configureSerialization()
    register(inject<IUserAuthService>().value)
    configureRouting(
        inject<HttpSignatureVerifyService>().value,
        inject<ActivityPubService>().value,
        inject<UserService>().value,
        inject<ActivityPubUserService>().value,
        inject<IPostService>().value
    )
}

@Suppress("unused")
fun Application.worker() {
    val kJob = kjob(ExposedKJob) {
        connectionDatabase = inject<Database>().value
    }.start()

    val activityPubService = inject<ActivityPubService>().value

    kJob.register(ReceiveFollowJob) {
        execute {
            activityPubService.processActivity(this, it)
        }
    }
}
