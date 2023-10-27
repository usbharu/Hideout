package dev.usbharu.hideout.service.ap.job

import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import kjob.core.job.JobProps

interface ApNoteJobService {
    suspend fun createNoteJob(props: JobProps<DeliverPostJob>)
}
