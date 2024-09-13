package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.timeline.AddTimelineRelationship
import dev.usbharu.hideout.core.application.timeline.UserAddTimelineRelationshipApplicationService
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventBody
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineRelationshipFollowSubscriber(
    private val userAddTimelineRelationshipApplicationService: UserAddTimelineRelationshipApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val domainEventSubscriber: DomainEventSubscriber
) : Subscriber, DomainEventConsumer<RelationshipEventBody> {

    override fun init() {
        domainEventSubscriber.subscribe<RelationshipEventBody>(RelationshipEvent.ACCEPT_FOLLOW.eventName, this)
    }


    override suspend fun invoke(p1: DomainEvent<RelationshipEventBody>) {
        val relationship = p1.body.getRelationship()
        val userDetail = userDetailRepository.findByActorId(relationship.actorId.id)
            ?: throw InternalServerException("Userdetail ${relationship.actorId} not found by actorid.")
        if (userDetail.homeTimelineId == null) {
            logger.warn("Home timeline for ${relationship.actorId} is not found")
            return
        }

        @Suppress("UnsafeCallOnNullableType")
        userAddTimelineRelationshipApplicationService.execute(
            AddTimelineRelationship(
                userDetail.homeTimelineId!!,
                relationship.targetActorId,
                Visible.FOLLOWERS
            ),
            p1.body.principal
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineRelationshipFollowSubscriber::class.java)
    }
}
