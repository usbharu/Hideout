package dev.usbharu.hideout.core.service.job

import dev.usbharu.hideout.core.external.job.HideoutJob

interface JobProcessorService<T, R : HideoutJob<T, R>> {
    suspend fun process(param: T)
    suspend fun job(): Class<R>
}
