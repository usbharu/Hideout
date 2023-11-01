package dev.usbharu.hideout.activitypub.service.`object`.note

import dev.usbharu.hideout.core.external.job.DeliverPostJob
import kjob.core.job.JobProps

interface ApNoteJobService {
    suspend fun createNoteJob(props: JobProps<DeliverPostJob>)
}
