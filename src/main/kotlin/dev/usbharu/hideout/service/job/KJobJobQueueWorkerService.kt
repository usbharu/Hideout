package dev.usbharu.hideout.service.job

import dev.usbharu.kjob.exposed.ExposedKJob
import kjob.core.Job
import kjob.core.dsl.JobContextWithProps
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.kjob
import org.jetbrains.exposed.sql.Database

class KJobJobQueueWorkerService(private val database: Database) : JobQueueWorkerService {

    val kjob by lazy {
        kjob(ExposedKJob) {
            connectionDatabase = database
            nonBlockingMaxJobs = 10
            blockingMaxJobs = 10
            jobExecutionPeriodInSeconds = 10
        }.start()
    }

    override fun init(defines: List<Pair<Job,JobRegisterContext<Job, JobContextWithProps<Job>>.(Job) -> KJobFunctions<Job, JobContextWithProps<Job>>>>) {
        defines.forEach { job ->
            kjob.register(job.first, job.second)
        }
    }

}
