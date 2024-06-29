package dev.usbharu.hideout.core.domain.model.followtimeline

interface FollowTimelineRepository {
    suspend fun save(followTimeline: FollowTimeline): FollowTimeline
    suspend fun delete(followTimeline: FollowTimeline)
}