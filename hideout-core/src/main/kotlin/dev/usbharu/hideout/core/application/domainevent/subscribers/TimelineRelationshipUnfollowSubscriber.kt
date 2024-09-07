package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.RemoveTimelineRelationship
import dev.usbharu.hideout.core.application.timeline.UserRemoveTimelineRelationshipApplicationService
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventBody
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineRelationshipUnfollowSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val userRemoveTimelineRelationshipApplicationService: UserRemoveTimelineRelationshipApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val timelineRelationshipRepository: TimelineRelationshipRepository,
) : Subscriber {
    override fun init() {
        domainEventSubscriber.subscribe<RelationshipEventBody>(RelationshipEvent.UNFOLLOW.eventName) {
            val relationship = it.body.getRelationship()
            val userDetail = userDetailRepository.findByActorId(relationship.actorId.id) ?: throw IllegalStateException(
                "UserDetail ${relationship.actorId} not found by actorId."
            )
            if (userDetail.homeTimelineId == null) {
                logger.warn("HomeTimeline for ${userDetail.id} not found.")
                return@subscribe
            }

            val timelineRelationship = timelineRelationshipRepository.findByTimelineIdAndActorId(
                userDetail.homeTimelineId!!,
                relationship.targetActorId
            )
                ?: throw IllegalStateException("TimelineRelationship ${userDetail.homeTimelineId} to ${relationship.targetActorId} not found by timelineId and ActorId")

            userRemoveTimelineRelationshipApplicationService.execute(
                RemoveTimelineRelationship(timelineRelationship.id),
                it.body.principal
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineRelationshipUnfollowSubscriber::class.java)
    }
}