package dev.usbharu.hideout.core.service.job

import dev.usbharu.hideout.core.external.job.HideoutJob

interface JobProcessor<out T, out R : HideoutJob<T, R>> {
    suspend fun process(param: @UnsafeVariance T)
    fun job(): R
}
