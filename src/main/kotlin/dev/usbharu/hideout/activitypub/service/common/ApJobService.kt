package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.core.external.job.HideoutJob
import kjob.core.dsl.JobContextWithProps

interface ApJobService {
    suspend fun <T, R : HideoutJob<T, R>> processActivity(job: JobContextWithProps<R>, hideoutJob: HideoutJob<T, R>)
}
