package dev.usbharu.hideout

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import dev.usbharu.hideout.domain.model.job.ReceiveFollowJob
import dev.usbharu.hideout.plugins.*
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.routing.register
import dev.usbharu.hideout.service.*
import dev.usbharu.hideout.service.activitypub.ActivityPubService
import dev.usbharu.hideout.service.activitypub.ActivityPubUserService
import dev.usbharu.hideout.service.impl.IUserService
import dev.usbharu.hideout.service.job.JobQueueParentService
import dev.usbharu.hideout.service.job.KJobJobQueueParentService
import dev.usbharu.hideout.service.signature.HttpSignatureVerifyService
import dev.usbharu.kjob.exposed.ExposedKJob
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import io.ktor.server.application.*
import kjob.core.kjob
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.koin.ksp.generated.module
import org.koin.ktor.ext.inject
import java.util.concurrent.TimeUnit

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

val Application.property: Application.(propertyName: String) -> String
    get() = {
        environment.config.property(it).getString()
    }

// application.conf references the main function. This annotation prevents the IDE from marking it as unused.
@Suppress("unused", "LongMethod")
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
        single<JobQueueParentService> {
            val kJobJobQueueService = KJobJobQueueParentService(get())
            kJobJobQueueService.init(emptyList())
            kJobJobQueueService
        }
        single<HttpClient> {
            HttpClient(CIO).config {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.INFO
                }
                install(httpSignaturePlugin) {
                    keyMap = KtorKeyMap(get())
                }
            }
        }
        single<IdGenerateService> { TwitterSnowflakeIdGenerateService }
        single<JwkProvider> {
            JwkProviderBuilder(Config.configData.url).cached(
                10,
                24,
                TimeUnit.HOURS
            )
                .rateLimited(10, 1, TimeUnit.MINUTES).build()
        }
    }
    configureKoin(module, HideoutModule().module)
    runBlocking {
        inject<IServerInitialiseService>().value.init()
    }
    configureHTTP()
    configureStaticRouting()
    configureMonitoring()
    configureSerialization()
    register(inject<IUserService>().value)
    configureSecurity(
        inject<IUserAuthService>().value,
        inject<IMetaService>().value,
        inject<IUserRepository>().value,
        inject<IJwtService>().value,
        inject<JwkProvider>().value,
    )
    configureRouting(
        httpSignatureVerifyService = inject<HttpSignatureVerifyService>().value,
        activityPubService = inject<ActivityPubService>().value,
        userService = inject<IUserService>().value,
        activityPubUserService = inject<ActivityPubUserService>().value,
        postService = inject<IPostService>().value,
        userApiService = inject<IUserApiService>().value,
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
    kJob.register(DeliverPostJob) {
        execute {
            activityPubService.processActivity(this, it)
        }
    }
}
