package dev.usbharu.hideout.service.job

import kjob.core.Job
import kjob.core.dsl.JobContextWithProps
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions

interface JobWorkerService {
    fun init(defines: List<Pair<Job, JobRegisterContext<Job, JobContextWithProps<Job>>.(Job) -> KJobFunctions<Job, JobContextWithProps<Job>>>>)
}
