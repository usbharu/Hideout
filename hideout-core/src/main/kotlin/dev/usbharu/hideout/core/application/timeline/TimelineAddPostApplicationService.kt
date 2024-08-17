package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineAddPostApplicationService(
    private val timelineStore: TimelineStore,
    private val postRepository: PostRepository,
    transaction: Transaction
) : AbstractApplicationService<AddPost, Unit>(
    transaction,
    logger
) {
    override suspend fun internalExecute(command: AddPost, principal: Principal) {
        val findById = postRepository.findById(command.postId)
            ?: throw IllegalArgumentException("Post ${command.postId} not found.")
        timelineStore.addPost(findById)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineAddPostApplicationService::class.java)
    }
}
