package dev.usbharu.hideout.core.domain.model.timeline

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

interface TimelineRepository {
    suspend fun save(timeline: Timeline): Timeline
    suspend fun delete(timeline: Timeline)

    suspend fun findByIds(ids: List<TimelineId>): List<Timeline>

    suspend fun findById(id: TimelineId): Timeline?

    @Suppress("FunctionMaxLength")
    suspend fun findAllByUserDetailIdAndVisibilityIn(
        userDetailId: UserDetailId,
        visibility: List<TimelineVisibility>
    ): List<Timeline>
}
