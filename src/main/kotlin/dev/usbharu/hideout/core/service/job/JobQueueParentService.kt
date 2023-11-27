package dev.usbharu.hideout.core.service.job

import dev.usbharu.hideout.core.external.job.HideoutJob
import kjob.core.Job
import kjob.core.dsl.ScheduleContext
import org.springframework.stereotype.Service

@Service
interface JobQueueParentService {

    fun init(jobDefines: List<Job>)

    @Deprecated("use type safe → scheduleTypeSafe")
    suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit = {})
    suspend fun <T, J : HideoutJob<T, J>> scheduleTypeSafe(job: J, jobProps: T)
}
