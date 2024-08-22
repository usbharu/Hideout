package dev.usbharu.hideout.core.application.domainevent.subscribers

import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEvent
import dev.usbharu.hideout.core.domain.event.userdetail.UserDetailEventBody
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import org.springframework.stereotype.Component

@Component
class RegisterLocalUserSetHomeTimelineSubscriber(
    domainEventSubscriber: DomainEventSubscriber,
    private val userRegisterHomeTimelineApplicationService: UserRegisterHomeTimelineApplicationService
) :
    Subscriber {
    init {
        domainEventSubscriber.subscribe<UserDetailEventBody>(UserDetailEvent.CREATE.eventName) {
            userRegisterHomeTimelineApplicationService.execute(
                RegisterHomeTimeline(it.body.getUserDetail().id),
                Anonymous
            )
        }
    }
}

// todo userdetailにdomain event付けて createのイベントで反応させる タイムラインを新しく一つ作って userdetailのhometimelineに紐づけて自分自身をtimleine relationshipに入れる