package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.support.timelineobjectdetail.TimelineObjectDetail
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectWarnFilter
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.timeline.TimelineStore
import java.time.Instant

abstract class AbstractTimelineStore(private val idGenerateService: IdGenerateService) : TimelineStore {
    override suspend fun addPost(post: Post) {
        val timelineList = getTimelines(post.actorId)

        val repost = post.repostId?.let { getPost(it) }
        val replyActorId = post.replyId?.let { getPost(it)?.actorId }

        val timelineObjectList = timelineList.mapNotNull {
            createTimelineObject(post, replyActorId, repost, it)
        }

        insertTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getTimelines(actorId: ActorId): List<Timeline>

    protected abstract suspend fun getTimeline(timelineId: TimelineId): Timeline?

    protected suspend fun createTimelineObject(
        post: Post,
        replyActorId: ActorId?,
        repost: Post?,
        timeline: Timeline
    ): TimelineObject? {
        if (post.visibility == Visibility.DIRECT) {
            return null
        }
        if (timeline.visibility == TimelineVisibility.PUBLIC && post.visibility != Visibility.PUBLIC) {
            return null
        }
        if (timeline.visibility == TimelineVisibility.UNLISTED && (post.visibility != Visibility.PUBLIC || post.visibility != Visibility.UNLISTED)) {
            return null
        }

        val filters = getFilters(timeline.userDetailId)

        val applyFilters = applyFilters(post, filters)

        if (repost != null) {
            return TimelineObject.create(
                TimelineObjectId(idGenerateService.generateId()),
                timeline,
                post,
                replyActorId,
                repost,
                applyFilters.filterResults
            )
        }

        return TimelineObject.create(
            TimelineObjectId(idGenerateService.generateId()), timeline, post, replyActorId, applyFilters.filterResults
        )
    }

    protected abstract suspend fun getFilters(userDetailId: UserDetailId): List<Filter>

    protected abstract suspend fun getNewerFilters(userDetailId: UserDetailId, lastUpdateAt: Instant): List<Filter>

    protected abstract suspend fun applyFilters(post: Post, filters: List<Filter>): FilteredPost

    protected abstract suspend fun getPost(postId: PostId): Post?

    protected abstract suspend fun insertTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun updateTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun getTimelineObjectByPostId(postId: PostId): List<TimelineObject>

    protected abstract suspend fun removeTimelineObject(postId: PostId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId, actorId: ActorId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId)

    protected abstract suspend fun getPostsByTimelineRelationshipList(timelineRelationshipList: List<TimelineRelationship>): List<Post>

    protected abstract suspend fun getPostsByPostId(postIds: List<PostId>): List<Post>

    protected abstract suspend fun getTimelineObject(timelineId: TimelineId): List<TimelineObject>

    override suspend fun updatePost(post: Post) {
        val timelineObjectByPostId = getTimelineObjectByPostId(post.id)

        val repost = post.repostId?.let { getPost(it) }

        val timelineObjectList = if (repost != null) {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, repost, applyFilters.filterResults)
                it
            }
        } else {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, applyFilters.filterResults)
                it
            }
        }

        updateTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getActorPost(actorId: ActorId, visibilityList: List<Visibility>): List<Post>

    override suspend fun removePost(post: Post) {
        removeTimelineObject(post.id)
    }

    override suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship) {
        val visibilityList = visibilities(timelineRelationship)
        val postList = getActorPost(timelineRelationship.actorId, visibilityList)
        val timeline = getTimeline(timelineRelationship.timelineId) ?: return
        val timelineObjects = postList.mapNotNull { post ->
            val repost = post.repostId?.let { getPost(it) }
            val replyActorId = post.replyId?.let { getPost(it)?.actorId }
            createTimelineObject(post, replyActorId, repost, timeline)
        }

        insertTimelineObject(timelineObjects)
    }

    protected fun visibilities(timelineRelationship: TimelineRelationship): List<Visibility> {
        val visibilityList = when (timelineRelationship.visible) {
            Visible.PUBLIC -> {
                listOf(Visibility.PUBLIC)
            }

            Visible.UNLISTED -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED)
            }

            Visible.FOLLOWERS -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED, Visibility.FOLLOWERS)
            }

            Visible.DIRECT -> {
                listOf(Visibility.PUBLIC, Visibility.UNLISTED, Visibility.FOLLOWERS, Visibility.DIRECT)
            }
        }
        return visibilityList
    }

    override suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship) {
        removeTimelineObject(timelineRelationship.timelineId, timelineRelationship.actorId)
    }

    override suspend fun updateTimelineRelationship(timelineRelationship: TimelineRelationship) {
        removeTimelineRelationship(timelineRelationship)
        addTimelineRelationship(timelineRelationship)
    }

    override suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>) {
        val postList = getPostsByTimelineRelationshipList(timelineRelationshipList)

        val timelineObjectList = postList.mapNotNull { post ->
            val repost = post.repostId?.let { getPost(it) }
            val replyActorId = post.replyId?.let { getPost(it)?.actorId }
            createTimelineObject(post, replyActorId, repost, timeline)
        }

        insertTimelineObject(timelineObjectList)
    }

    override suspend fun removeTimeline(timeline: Timeline) {
        removeTimelineObject(timeline.id)
    }

    override suspend fun readTimeline(timeline: Timeline): List<TimelineObjectDetail> {
        val timelineObjectList = getTimelineObject(timeline.id)
        val lastUpdatedAt = timelineObjectList.minBy { it.lastUpdatedAt }.lastUpdatedAt

        val newerFilters = getNewerFilters(timeline.userDetailId, lastUpdatedAt)

        val posts =
            getPostsByPostId(timelineObjectList.map { it.postId } + timelineObjectList.mapNotNull { it.repostId } + timelineObjectList.mapNotNull { it.replyId })

        val userDetails = getUserDetails(timelineObjectList.map { it.userDetailId })

        val actors =
            getActors(timelineObjectList.map { it.postActorId } + timelineObjectList.mapNotNull { it.repostActorId } + timelineObjectList.mapNotNull { it.replyActorId })

        val postMap = posts.associate { post ->
            post.id to applyFilters(post, newerFilters)
        }

        return timelineObjectList.mapNotNull<TimelineObject, TimelineObjectDetail> {
            val timelineUserDetail = userDetails[it.userDetailId] ?: return@mapNotNull null
            val actor = actors[it.postActorId] ?: return@mapNotNull null
            val post = postMap[it.postId] ?: return@mapNotNull null
            val reply = postMap[it.replyId]
            val replyActor = actors[it.replyActorId]
            val repost = postMap[it.repostId]
            val repostActor = actors[it.repostActorId]
            TimelineObjectDetail.of(
                timelineObject = it,
                timelineUserDetail = timelineUserDetail,
                post = post.post,
                postActor = actor,
                replyPost = reply?.post,
                replyPostActor = replyActor,
                repostPost = repost?.post,
                repostPostActor = repostActor,
                warnFilter = it.warnFilters + post.filterResults.map {
                    TimelineObjectWarnFilter(
                        it.filter.id,
                        it.matchedKeyword
                    )
                }
            )
        }
    }

    abstract suspend fun getActors(actorIds: List<ActorId>): Map<ActorId, Actor>

    abstract suspend fun getUserDetails(userDetailIdList: List<UserDetailId>): Map<UserDetailId, UserDetail>
}