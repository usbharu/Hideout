package dev.usbharu.hideout.core.application.timeline

import dev.usbharu.hideout.core.application.post.PostDetail
import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.external.timeline.ReadTimelineOption
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ReadTimelineApplicationService(
    private val timelineStore: TimelineStore,
    private val timelineRepository: TimelineRepository,
    transaction: Transaction
) :
    AbstractApplicationService<ReadTimeline, PaginationList<PostDetail, PostId>>(transaction, logger) {
    override suspend fun internalExecute(
        command: ReadTimeline,
        principal: Principal
    ): PaginationList<PostDetail, PostId> {
        val findById = timelineRepository.findById(TimelineId(command.timelineId))
            ?: throw IllegalArgumentException("Timeline ${command.timelineId} not found.")

        val readTimelineOption = ReadTimelineOption(
            command.mediaOnly,
            command.localOnly,
            command.remoteOnly
        )

        val timeline = timelineStore.readTimeline(
            findById,
            readTimelineOption,
            command.page,
            principal,
        )

        val postDetailList = timeline.map {
            val reply = if (it.replyPost != null) {
                @Suppress("UnsafeCallOnNullableType")
                PostDetail.of(
                    it.replyPost,
                    it.replyPostActor!!,
                    it.replyPostActorIconMedia,
                    it.replyPostMedias.orEmpty(),
                    reactionsList = emptyList(),
                    favourited = false,
                )
            } else {
                null
            }

            val repost = if (it.repostPost != null) {
                @Suppress("UnsafeCallOnNullableType")
                PostDetail.of(
                    post = it.repostPost,
                    actor = it.repostPostActor!!,
                    iconMedia = it.repostPostActorIconMedia,
                    mediaList = it.repostPostMedias.orEmpty(),
                    reactionsList = emptyList(),
                    favourited = false
                )
            } else {
                null
            }

            PostDetail.of(
                post = it.post,
                actor = it.postActor,
                iconMedia = it.postActorIconMedia,
                mediaList = it.postMedias,
                reply = reply,
                repost = repost,
                reactionsList = emptyList(),
                favourited = it.favourited
            )
        }

        return PaginationList(postDetailList, timeline.next, timeline.prev)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReadTimelineApplicationService::class.java)
    }
}
