package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.config.DefaultTimelineStoreConfig
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.service.filter.FilterDomainService
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.springframework.stereotype.Component

@Component
open class DefaultTimelineStore(
    private val timelineRepository: TimelineRepository,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    private val filterRepository: FilterRepository,
    private val postRepository: PostRepository,
    private val filterDomainService: FilterDomainService,
    idGenerateService: IdGenerateService,
    private val defaultTimelineStoreConfig: DefaultTimelineStoreConfig,
    private val internalTimelineObjectRepository: InternalTimelineObjectRepository
) : AbstractTimelineStore(idGenerateService) {
    override suspend fun getTimelines(actorId: ActorId): List<Timeline> {
        return timelineRepository.findByIds(
            timelineRelationshipRepository
                .findByActorId(
                    actorId
                ).map { it.timelineId }
        )
    }

    override suspend fun getTimeline(timelineId: TimelineId): Timeline? {
        return timelineRepository.findById(timelineId)
    }

    override suspend fun getFilters(userDetailId: UserDetailId): List<Filter> {
        return filterRepository.findByUserDetailId(userDetailId)
    }

    override suspend fun applyFilters(post: Post, filters: List<Filter>): FilteredPost {
        return filterDomainService.apply(post, FilterContext.HOME, filters)
    }

    override suspend fun getPost(postId: PostId): Post? {
        return postRepository.findById(postId)
    }

    override suspend fun insertTimelineObject(timelineObjectList: List<TimelineObject>) {
        internalTimelineObjectRepository.saveAll(timelineObjectList)
    }

    override suspend fun updateTimelineObject(timelineObjectList: List<TimelineObject>) {
        internalTimelineObjectRepository.saveAll(timelineObjectList)
    }

    override suspend fun getTimelineObjectByPostId(postId: PostId): List<TimelineObject> {
        return internalTimelineObjectRepository.findByPostId(postId)
    }

    override suspend fun removeTimelineObject(postId: PostId) {
        internalTimelineObjectRepository.deleteByPostId(postId)
    }

    override suspend fun removeTimelineObject(timelineId: TimelineId, actorId: ActorId) {
        internalTimelineObjectRepository.deleteByTimelineIdAndActorId(timelineId, actorId)
    }

    override suspend fun removeTimelineObject(timelineId: TimelineId) {
        internalTimelineObjectRepository.deleteByTimelineId(timelineId)
    }

    override suspend fun getPosts(timelineRelationshipList: List<TimelineRelationship>): List<Post> {
        return timelineRelationshipList.map { it.actorId }.flatMap { getActorPost(it) }
    }

    override suspend fun getActorPost(actorId: ActorId): List<Post> {
        return postRepository.findByActorId(actorId, Page.of(limit = defaultTimelineStoreConfig.actorPostsCount))
    }
}