package dev.usbharu.hideout.core.infrastructure.kjobexposed

import kjob.core.BaseKJob
import kjob.core.KJob
import kjob.core.KJobFactory
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import java.time.Clock

class ExposedKJob(config: Configuration) : BaseKJob<ExposedKJob.Configuration>(config) {

    private val database: Database = config.connectionDatabase ?: Database.connect(
        requireNotNull(config.connectionString),
        requireNotNull(config.driverClassName)
    )

    override val jobRepository: ExposedJobRepository
        get() = ExposedJobRepository(database, config.jobTableName, Clock.systemUTC(), config.json)

    override val lockRepository: ExposedLockRepository
        get() = ExposedLockRepository(database, config, clock)

    override fun start(): KJob {
        jobRepository.createTable()
        lockRepository.createTable()
        return super.start()
    }

    override fun shutdown(): Unit = runBlocking {
        super.shutdown()
        lockRepository.clearExpired()
    }

    companion object : KJobFactory<ExposedKJob, Configuration> {
        override fun create(configure: Configuration.() -> Unit): KJob = ExposedKJob(Configuration().apply(configure))
    }

    class Configuration : BaseKJob.Configuration() {
        var connectionString: String? = null
        var driverClassName: String? = null
        var connectionDatabase: Database? = null

        var jobTableName: String = "kjobJobs"

        var lockTableName: String = "kjobLocks"

        var expireLockInMinutes: Long = 5L
    }
}
