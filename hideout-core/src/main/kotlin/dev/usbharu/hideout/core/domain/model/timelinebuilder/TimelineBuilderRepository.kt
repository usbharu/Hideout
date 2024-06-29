package dev.usbharu.hideout.core.domain.model.timelinebuilder

interface TimelineBuilderRepository {
    suspend fun save(timelineBuilder: TimelineBuilder): TimelineBuilder
    suspend fun delete(timelineBuilder: TimelineBuilder)
}