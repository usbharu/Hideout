package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.job.DeliverPostJob
import kjob.core.job.JobProps

interface ActivityPubNoteService {

    suspend fun createNote(post: Post)
    suspend fun createNoteJob(props: JobProps<DeliverPostJob>)
}
