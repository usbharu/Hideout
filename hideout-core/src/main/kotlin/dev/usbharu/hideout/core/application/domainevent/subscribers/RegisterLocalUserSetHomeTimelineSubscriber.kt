package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEvent
import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import org.springframework.stereotype.Component

@Component
class RegisterLocalUserSetHomeTimelineSubscriber(
    private val domainEventSubscriber: DomainEventSubscriber,
    private val userRegisterHomeTimelineApplicationService: UserRegisterHomeTimelineApplicationService
) : Subscriber, DomainEventConsumer<UserDetailEventBody> {
    override fun init() {
        domainEventSubscriber.subscribe<UserDetailEventBody>(UserDetailEvent.CREATE.eventName, this)
    }

    override suspend fun invoke(p1: DomainEvent<UserDetailEventBody>) {
        userRegisterHomeTimelineApplicationService.execute(
            RegisterHomeTimeline(p1.body.getUserDetail().id),
            Anonymous
        )
    }
}
