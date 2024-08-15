package dev.usbharu.hideout.core.application.domainevent.subscribers

class RegisterLocalUserSetHomeTimelineSubscriber(private val domainEventSubscriber: DomainEventSubscriber) :
    Subscriber {
    init {
        domainEventSubscriber.subscribe<>()
    }
}

//todo userdetailにdomain event付けて createのイベントで反応させる タイムラインを新しく一つ作って userdetailのhometimelineに紐づけて自分自身をtimleine relationshipに入れる