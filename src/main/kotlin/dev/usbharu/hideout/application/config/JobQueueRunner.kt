package dev.usbharu.hideout.application.config

import dev.usbharu.hideout.activitypub.service.common.ApJobService
import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import dev.usbharu.hideout.core.service.job.JobQueueWorkerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JobQueueRunner(
    private val jobQueueParentService: JobQueueParentService,
    private val jobs: List<HideoutJob<*, *>>
) :
    ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue. ${jobs.size}")
        jobQueueParentService.init(jobs)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(JobQueueRunner::class.java)
    }
}

@Component
class JobQueueWorkerRunner(
    private val jobQueueWorkerService: JobQueueWorkerService,
    private val jobs: List<HideoutJob<*, *>>,
    private val apJobService: ApJobService
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue worker.")
//        jobQueueWorkerService.init<Any?, HideoutJob<*, *>>(
//            jobs.map {
//                it to {
//                    execute {
//                        LOGGER.debug("excute job ${it.name}")
//                        apJobService.processActivity(
//                            job = this,
//                            hideoutJob = it
//                        )
//                    }
//                }
//            }
//        )
        jobQueueWorkerService.init<Any?, HideoutJob<*, *>>(emptyList())
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(JobQueueWorkerRunner::class.java)
    }
}
