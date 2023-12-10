package dev.usbharu.hideout.core.infrastructure.kjobexposed

import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.core.service.job.JobQueueWorkerService
import kjob.core.dsl.JobContextWithProps
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.kjob
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "false", matchIfMissing = true)
class KJobJobQueueWorkerService(private val jobQueueProcessorList: List<JobProcessor<*, *>>) : JobQueueWorkerService {

    val kjob by lazy {
        kjob(ExposedKJob) {
            connectionDatabase = TransactionManager.defaultDatabase
            nonBlockingMaxJobs = 10
            blockingMaxJobs = 10
            jobExecutionPeriodInSeconds = 1
        }.start()
    }

    override fun <T, R : HideoutJob<T, R>> init(
        defines:
        List<Pair<R, JobRegisterContext<R, JobContextWithProps<R>>.(R) -> KJobFunctions<R, JobContextWithProps<R>>>>
    ) {
        defines.forEach { job ->
            kjob.register(job.first, job.second)
        }

        for (jobProcessor in jobQueueProcessorList) {
            kjob.register(jobProcessor.job()) {
                execute {
                    try {
                        MDC.put("x-job-id", this.jobId)
                        val param = it.convertUnsafe(props)
                        jobProcessor.process(param)
                    } catch (e: Exception) {
                        logger.warn("FAILED Execute Job. job name: {} job id: {}", it.name, this.jobId, e)
                        throw e
                    } finally {
                        MDC.remove("x-job-id")
                    }
                }
            }
        }
    }
}
