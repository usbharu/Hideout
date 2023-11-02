package dev.usbharu.hideout.core.infrastructure.kjobmongodb

import dev.usbharu.hideout.core.service.job.JobQueueWorkerService
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.kjob
import kjob.mongo.Mongo
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import dev.usbharu.hideout.core.external.job.HideoutJob as HJ
import kjob.core.dsl.JobContextWithProps as JCWP

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "true", matchIfMissing = false)
class KJobMongoJobQueueWorkerService : JobQueueWorkerService {
    val kjob by lazy {
        kjob(Mongo) {
            connectionString = "mongodb://localhost"
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