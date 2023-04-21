package dev.usbharu.hideout.service.job

import dev.usbharu.kjob.exposed.ExposedKJob
import kjob.core.Job
import kjob.core.KJob
import kjob.core.dsl.ScheduleContext
import kjob.core.kjob
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

class KJobJobQueueParentService(private val database: Database) : JobQueueParentService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    val kjob: KJob = kjob(ExposedKJob) {
        connectionDatabase =  database
        isWorker = false
    }.start()

    override fun init(jobDefines: List<Job>) {

    }

    override suspend fun <J : Job> schedule(job: J,block:ScheduleContext<J>.(J)->Unit) {
        logger.debug("schedule job={}",job.name)
        kjob.schedule(job,block)
    }
}
