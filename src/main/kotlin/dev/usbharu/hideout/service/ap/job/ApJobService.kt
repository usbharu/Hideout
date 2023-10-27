package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.domain.model.job.HideoutJob
import kjob.core.dsl.JobContextWithProps

interface ApJobService {
    suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob)
}
