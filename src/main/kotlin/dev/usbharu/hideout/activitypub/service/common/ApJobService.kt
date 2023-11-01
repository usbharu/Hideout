package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.core.external.job.HideoutJob
import kjob.core.dsl.JobContextWithProps

interface ApJobService {
    suspend fun <T : HideoutJob> processActivity(job: JobContextWithProps<T>, hideoutJob: HideoutJob)
}
