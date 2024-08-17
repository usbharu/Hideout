package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.config.DefaultTimelineStoreConfig
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.media.Media
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.media.MediaRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.page.Page
import dev.usbharu.hideout.core.domain.model.support.page.PaginationList
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail.TimelineObjectDetail
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
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.timeline.ReadTimelineOption
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
    private val actorRepository: ActorRepository,
    private val mediaRepository: MediaRepository,
    private val postIPostReadAccessControl: IPostReadAccessControl
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

    override suspend fun getPostsByTimelineRelationshipList(timelineRelationshipList: List<TimelineRelationship>): List<Post> {
        return timelineRelationshipList.flatMap { getActorPost(it.actorId, visibilities(it)) }
    }

    override suspend fun getPostsByPostId(postIds: List<PostId>, principal: Principal): List<Post> {
        val findAllById = postRepository.findAllById(postIds)
        return postIPostReadAccessControl.areAllows(findAllById, principal)
    }

    override suspend fun getTimelineObject(
        timelineId: TimelineId,
        readTimelineOption: ReadTimelineOption?,
        page: Page?
    ): PaginationList<TimelineObject, PostId> {
        return internalTimelineObjectRepository.findByTimelineId(
            timelineId,
            InternalTimelineObjectOption(
                readTimelineOption?.local,
                readTimelineOption?.remote,
                readTimelineOption?.mediaOnly
            ),
            page
        )
    }

    override suspend fun getActorPost(actorId: ActorId, visibilityList: List<Visibility>): List<Post> {
        return postRepository.findByActorIdAndVisibilityInList(
            actorId,
            visibilityList,
            Page.of(limit = defaultTimelineStoreConfig.actorPostsCount)
        )
    }

    override suspend fun getNextPaging(
        timelineId: TimelineId,
        page: Page?
    ): PaginationList<TimelineObjectDetail, PostId> {
        if (page?.maxId != null) {
            return PaginationList(
                emptyList(),
                null,
                internalTimelineObjectRepository.findByTimelineIdAndPostIdLT(timelineId, PostId(page.maxId!!))?.postId
                    ?: PostId(0)
            )
        } else if (page?.minId != null) {
            return PaginationList(
                emptyList(),
                internalTimelineObjectRepository.findByTimelineIdAndPostIdGT(timelineId, PostId(page.minId!!))?.postId
                    ?: PostId(Long.MAX_VALUE),
                null
            )
        }
        return PaginationList(emptyList(), page?.maxId?.let { PostId(it) }, page?.minId?.let { PostId(it) })
    }

    override suspend fun getActors(actorIds: List<ActorId>): Map<ActorId, Actor> {
        return actorRepository.findAllById(actorIds).associateBy { it.id }
    }

    override suspend fun getMedias(mediaIds: List<MediaId>): Map<MediaId, Media> {
        return mediaRepository.findByIds(mediaIds).associateBy { it.id }
    }

    override suspend fun getUserDetails(userDetailIdList: List<UserDetailId>): Map<UserDetailId, UserDetail> {
        return userDetailRepository.findAllById(userDetailIdList).associateBy { it.id }
    }
}
