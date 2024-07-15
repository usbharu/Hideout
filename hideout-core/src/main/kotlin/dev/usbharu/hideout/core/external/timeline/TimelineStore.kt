package dev.usbharu.hideout.core.external.timeline

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship

interface TimelineStore {
    suspend fun addPost(post: Post)
    suspend fun updatePost(post: Post)
    suspend fun removePost(post: Post)
    suspend fun addTimelineRelationship(timelineRelationship: TimelineRelationship)
    suspend fun removeTimelineRelationship(timelineRelationship: TimelineRelationship)
    suspend fun addTimeline(timeline: Timeline, timelineRelationshipList: List<TimelineRelationship>)
    suspend fun removeTimeline(timeline: Timeline)
}