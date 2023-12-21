package dev.usbharu.hideout.core.infrastructure.kjobmongodb

import com.mongodb.reactivestreams.client.MongoClient
import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobProcessor
import dev.usbharu.hideout.core.service.job.JobQueueWorkerService
import kjob.core.dsl.JobContextWithProps
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.job.JobExecutionType
import kjob.core.kjob
import kjob.mongo.Mongo
import kotlinx.coroutines.CancellationException
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "true", matchIfMissing = false)
class KJobMongoJobQueueWorkerService(
    private val mongoClient: MongoClient,
    private val jobQueueProcessorList: List<JobProcessor<*, *>>
) : JobQueueWorkerService, AutoCloseable {
    val kjob by lazy {
        kjob(Mongo) {
            client = mongoClient
            nonBlockingMaxJobs = 10
            blockingMaxJobs = 10
            jobExecutionPeriodInSeconds = 1
            maxRetries = 3
            defaultJobExecutor = JobExecutionType.NON_BLOCKING
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
                    @Suppress("TooGenericExceptionCaught")
                    try {
                        MDC.put("x-job-id", this.jobId)
                        val param = it.convertUnsafe(props)
                        jobProcessor.process(param)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        logger.warn("FAILED Excute Job. job name: {} job id: {}", it.name, this.jobId, e)
                        throw e
                    } finally {
                        MDC.remove("x-job-id")
                    }
                }.onError {
                    logger.warn("FAILED Excute Job. job name: {} job id: {}", this.jobName, this.jobId, error)
                }
            }
        }
    }

    override fun close() {
        kjob.shutdown()
    }
}
