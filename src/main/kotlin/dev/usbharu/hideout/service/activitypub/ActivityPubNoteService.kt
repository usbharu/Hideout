package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.PostEntity
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import kjob.core.job.JobProps

interface ActivityPubNoteService {

    suspend fun createNote(post:PostEntity)
    suspend fun createNoteJob(props:JobProps<DeliverPostJob>)
}
