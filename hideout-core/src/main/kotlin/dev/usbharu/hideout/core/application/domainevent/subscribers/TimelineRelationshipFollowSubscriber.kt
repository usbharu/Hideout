package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.exception.InternalServerException
import dev.usbharu.hideout.core.application.timeline.AddTimelineRelationship
import dev.usbharu.hideout.core.application.timeline.UserAddTimelineRelationshipApplicationService
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventBody
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineRelationshipFollowSubscriber(
    private val userAddTimelineRelationshipApplicationService: UserAddTimelineRelationshipApplicationService,
    private val userDetailRepository: UserDetailRepository,
    private val domainEventSubscriber: DomainEventSubscriber
) : Subscriber {

    override fun init() {
        domainEventSubscriber.subscribe<RelationshipEventBody>(RelationshipEvent.ACCEPT_FOLLOW.eventName) {
            val relationship = it.body.getRelationship()
            val userDetail = userDetailRepository.findByActorId(relationship.actorId.id)
                ?: throw InternalServerException("Userdetail ${relationship.actorId} not found by actorid.")
            if (userDetail.homeTimelineId == null) {
                logger.warn("Home timeline for ${relationship.actorId} is not found")
                return@subscribe
            }

            @Suppress("UnsafeCallOnNullableType")
            userAddTimelineRelationshipApplicationService.execute(
                AddTimelineRelationship(
                    userDetail.homeTimelineId!!,
                    relationship.targetActorId,
                    Visible.FOLLOWERS
                ),
                it.body.principal
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineRelationshipFollowSubscriber::class.java)
    }
}
