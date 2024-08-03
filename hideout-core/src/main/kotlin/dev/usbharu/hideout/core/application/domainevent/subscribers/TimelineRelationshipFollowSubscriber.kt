package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.application.timeline.AddTimelineRelationship
import dev.usbharu.hideout.core.application.timeline.UserAddTimelineRelationshipApplicationService
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEvent
import dev.usbharu.hideout.core.domain.event.relationship.RelationshipEventBody
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationship
import dev.usbharu.hideout.core.domain.model.timelinerelationship.TimelineRelationshipId
import dev.usbharu.hideout.core.domain.model.timelinerelationship.Visible
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TimelineRelationshipFollowSubscriber(
    private val userAddTimelineRelationshipApplicationService: UserAddTimelineRelationshipApplicationService,
    private val idGenerateService: IdGenerateService,
    private val userDetailRepository: UserDetailRepository,
    domainEventSubscriber: DomainEventSubscriber
) : Subscriber {

    init {
        domainEventSubscriber.subscribe<RelationshipEventBody>(RelationshipEvent.FOLLOW.eventName) {
            val relationship = it.body.getRelationship()
            val userDetail = userDetailRepository.findByActorId(relationship.actorId.id) ?: throw Exception()
            if (userDetail.homeTimelineId == null) {
                logger.warn("Home timeline for ${relationship.actorId} is not found")
                return@subscribe
            }
            userAddTimelineRelationshipApplicationService.execute(
                AddTimelineRelationship(
                    TimelineRelationship(
                        TimelineRelationshipId(idGenerateService.generateId()),
                        userDetail.homeTimelineId,
                        relationship.targetActorId,
                        Visible.FOLLOWERS
                    )
                )
            )


        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TimelineRelationshipFollowSubscriber::class.java)
    }

}