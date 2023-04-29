package dev.usbharu.hideout.service.job

import kjob.core.Job
import kjob.core.dsl.KJobFunctions
import kjob.core.dsl.JobContextWithProps as JCWP
import kjob.core.dsl.JobRegisterContext as JRC

interface JobQueueWorkerService {
    fun init(defines: List<Pair<Job, JRC<Job, JCWP<Job>>.(Job) -> KJobFunctions<Job, JCWP<Job>>>>)
}
