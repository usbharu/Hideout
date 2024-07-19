package dev.usbharu.hideout.core.infrastructure.timeline

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObject
import dev.usbharu.hideout.core.domain.model.timelineobject.TimelineObjectId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import dev.usbharu.hideout.core.external.timeline.TimelineStore

abstract class AbstractTimelineStore(private val idGenerateService: IdGenerateService) : TimelineStore {
    override suspend fun addPost(post: Post) {
        val timelineList = getTimelines(post.actorId)

        val repost = post.repostId?.let { getPost(it) }

        val timelineObjectList = timelineList.map {
            createTimelineObject(post, repost, it)
        }

        insertTimelineObject(timelineObjectList)
    }

    protected abstract suspend fun getTimelines(actorId: ActorId): List<Timeline>

    protected abstract suspend fun getTimeline(timelineId: TimelineId): Timeline?

    protected suspend fun createTimelineObject(post: Post, repost: Post?, timeline: Timeline): TimelineObject {
        val filters = getFilters(timeline.userDetailId)

        val applyFilters = applyFilters(post, filters)

        if (repost != null) {
            return TimelineObject.create(
                TimelineObjectId(idGenerateService.generateId()), timeline, post, repost, applyFilters.filterResults
            )
        }

        return TimelineObject.create(
            TimelineObjectId(idGenerateService.generateId()), timeline, post, applyFilters.filterResults
        )
    }

    protected abstract suspend fun getFilters(userDetailId: UserDetailId): List<Filter>

    protected abstract suspend fun applyFilters(post: Post, filters: List<Filter>): FilteredPost

    protected abstract suspend fun getPost(postId: PostId): Post?

    protected abstract suspend fun insertTimelineObject(timelineObjectList: List<TimelineObject>)

    protected abstract suspend fun getTimelineObjectByPostId(postId: PostId): List<TimelineObject>

    protected abstract suspend fun removeTimelineObject(postId: PostId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId, actorId: ActorId)

    protected abstract suspend fun removeTimelineObject(timelineId: TimelineId)

    protected abstract suspend fun getPosts(timelineRelationshipList: List<TimelineRelationship>): List<Post>

    override suspend fun updatePost(post: Post) {


        val timelineObjectByPostId = getTimelineObjectByPostId(post.id)

        val repost = post.repostId?.let { getPost(it) }

        if (repost != null) {
            timelineObjectByPostId.map {
                val filters = getFilters(it.userDetailId)
                val applyFilters = applyFilters(post, filters)
                it.updateWith(post, repost, applyFilters.filterResults)
            }
        }
    }

    protected abstract suspend fun getActorPost(actorId: ActorId): List<Post>

    override suspend fun removePost(post: Post) {
        removeTimelineObject(post.id)
    }

    override suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship) {
        val postList = getActorPost(timelineRelationship.actorId)
        val timeline = getTimeline(timelineRelationship.timelineId) ?: return
        val timelineObjects = postList.map { post ->
            val repost = post.repostId?.let { getPost(it) }
            createTimelineObject(post, repost, timeline)
        }

        insertTimelineObject(timelineObjects)
    }

    override suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship) {
        removeTimelineObject(timelineRelationship.timelineId, timelineRelationship.actorId)
    }

    override suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>) {
        val postList = getPosts(timelineRelationshipList)

        val timelineObjectList = postList.map { post ->
            val repost = post.repostId?.let { getPost(it) }
            createTimelineObject(post, repost, timeline)
        }

        insertTimelineObject(timelineObjectList)
    }

    override suspend fun removeTimeline(timeline: Timeline) {
        removeTimelineObject(timeline.id)
    }
}