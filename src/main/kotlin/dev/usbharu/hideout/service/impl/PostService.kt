package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.repository.IPostRepository
import dev.usbharu.hideout.service.IPostService
import dev.usbharu.hideout.service.job.JobQueueParentService

class PostService(private val postRepository:IPostRepository,private val jobQueueParentService: JobQueueParentService) : IPostService {
    override suspend fun create(post: Post) {
        postRepository.insert(post)

    }
}
