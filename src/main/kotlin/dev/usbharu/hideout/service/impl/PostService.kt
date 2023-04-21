package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
import dev.usbharu.hideout.service.job.JobQueueParentService

class PostService(private val postRepository:IPostRepository,private val activityPubNoteService: ActivityPubNoteService) : IPostService {
    override suspend fun create(post: Post) {
        val postEntity = postRepository.insert(post)
        activityPubNoteService.createNote(postEntity)
    }
}
