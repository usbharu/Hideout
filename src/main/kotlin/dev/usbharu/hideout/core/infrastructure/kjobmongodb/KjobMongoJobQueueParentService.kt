package dev.usbharu.hideout.core.infrastructure.kjobmongodb

import dev.usbharu.hideout.core.service.job.JobQueueParentService
import kjob.core.Job
import kjob.core.dsl.ScheduleContext
import kjob.core.kjob
import kjob.mongo.Mongo
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(name = ["hideout.use-mongodb"], havingValue = "true", matchIfMissing = false)
class KjobMongoJobQueueParentService : JobQueueParentService {
    private val kjob = kjob(Mongo) {
        connectionString = "mongodb://localhost"
        databaseName = "kjob"
        jobCollection = "kjob-jobs"
        lockCollection = "kjob-locks"
        expireLockInMinutes = 5L
        isWorker = false
    }.start()

    override fun init(jobDefines: List<Job>) = Unit

    override suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit) {
        kjob.schedule(job, block)
    }
}
