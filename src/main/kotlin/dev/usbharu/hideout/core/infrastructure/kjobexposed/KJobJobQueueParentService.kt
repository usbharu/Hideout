package dev.usbharu.hideout.core.infrastructure.kjobexposed

import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import kjob.core.Job
import kjob.core.KJob
import kjob.core.dsl.ScheduleContext
import kjob.core.kjob
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "false", matchIfMissing = true)
class KJobJobQueueParentService : JobQueueParentService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val kjob: KJob by lazy {
        kjob(ExposedKJob) {
            connectionDatabase = TransactionManager.defaultDatabase
            isWorker = false
        }.start()
    }

    override fun init(jobDefines: List<Job>) = Unit

    override suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit) {
        logger.debug("schedule job={}", job.name)
        kjob.schedule(job, block)
    }

    override suspend fun <T, J : HideoutJob<T, J>> scheduleTypeSafe(job: J, jobProps: T) {
        logger.debug("SCHEDULE Job: {}", job.name)
        logger.trace("Job props: {}", jobProps)
        val convert: ScheduleContext<J>.(J) -> Unit = job.convert(jobProps)
        kjob.schedule(job, convert)
        logger.debug("SUCCESS Schedule Job: {}", job.name)
    }
}
