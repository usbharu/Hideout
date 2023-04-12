package dev.usbharu.hideout.service.job

import kjob.core.Job
import kjob.core.dsl.ScheduleContext

interface JobQueueParentService {

    fun init(jobDefines:List<Job>)
    suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit = {})
}
