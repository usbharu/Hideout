package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.config.DefaultTimelineStoreConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineRepository
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.service.filter.FilterDomainService
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
open class DefaultTimelineStore(
    private val timelineRepository: TimelineRepository,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
    private val filterRepository: FilterRepository,
    private val postRepository: PostRepository,
    private val filterDomainService: FilterDomainService,
    idGenerateService: IdGenerateService,
    private val defaultTimelineStoreConfig: DefaultTimelineStoreConfig,
    private val internalTimelineObjectRepository: InternalTimelineObjectRepository,
    private val userDetailRepository: UserDetailRepository,
    private val actorRepository: ActorRepository
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

    override suspend fun getNewerFilters(userDetailId: UserDetailId, lastUpdateAt: Instant): List<Filter> {
        TODO("Not yet implemented")
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

    override suspend fun getPostsByTimelineRelationshipList(timelineRelationshipList: List<TimelineRelationship>): List<Post> {
        return timelineRelationshipList.flatMap { getActorPost(it.actorId, visibilities(it)) }
    }

    override suspend fun getPostsByPostId(postIds: List<PostId>): List<Post> {
        return postRepository.findAllById(postIds)
    }

    override suspend fun getTimelineObject(timelineId: TimelineId): List<TimelineObject> {
        return internalTimelineObjectRepository.findByTimelineId(timelineId)
    }

    override suspend fun getActorPost(actorId: ActorId, visibilityList: List<Visibility>): List<Post> {
        return postRepository.findByActorIdAndVisibilityInList(
            actorId,
            visibilityList,
            Page.of(limit = defaultTimelineStoreConfig.actorPostsCount)
        )
    }

    override suspend fun getActors(actorIds: List<ActorId>): Map<ActorId, Actor> {
        return actorRepository.findAllById(actorIds).associateBy { it.id }
    }

    override suspend fun getUserDetails(userDetailIdList: List<UserDetailId>): Map<UserDetailId, UserDetail> {
        return userDetailRepository.findAllById(userDetailIdList).associateBy { it.id }
    }
}
