package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.activitypub.ActivityPubNoteService
import dev.usbharu.hideout.service.job.JobQueueParentService
import org.slf4j.LoggerFactory

class PostService(private val postRepository:IPostRepository,private val activityPubNoteService: ActivityPubNoteService) : IPostService {

    private val logger = LoggerFactory.getLogger(this::class.java)
    override suspend fun create(post: Post) {
        logger.debug("create post={}",post)
        val postEntity = postRepository.insert(post)
        activityPubNoteService.createNote(postEntity)
    }
}
