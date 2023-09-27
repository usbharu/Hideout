package dev.usbharu.hideout.service.job

import dev.usbharu.kjob.exposed.ExposedKJob
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.kjob
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import dev.usbharu.hideout.domain.model.job.HideoutJob as HJ
import kjob.core.dsl.JobContextWithProps as JCWP

@Service
@ConditionalOnProperty(name = ["hideout.job-queue.type"], havingValue = "rdb", matchIfMissing = true)
class KJobJobQueueWorkerService(private val database: Database) : JobQueueWorkerService {

    val kjob by lazy {
        kjob(ExposedKJob) {
            connectionDatabase = database
            nonBlockingMaxJobs = 10
            blockingMaxJobs = 10
            jobExecutionPeriodInSeconds = 1
        }.start()
    }

    override fun init(
        defines: List<Pair<HJ, JobRegisterContext<HJ, JCWP<HJ>>.(HJ) -> KJobFunctions<HJ, JCWP<HJ>>>>
    ) {
        defines.forEach { job ->
            kjob.register(job.first, job.second)
        }
    }
}
