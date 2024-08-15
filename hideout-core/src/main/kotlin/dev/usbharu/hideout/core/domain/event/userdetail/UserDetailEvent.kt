package dev.usbharu.hideout.core.domain.event.userdetail

import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEvent
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventBody

class UserDetailDomainEventFactory(private val userDetail: UserDetail) {
    fun createEvent(userDetailEvent: UserDetailEvent): DomainEvent<UserDetailEventBody> {
        return DomainEvent.create(
            userDetailEvent.eventName,
            UserDetailEventBody(userDetail.id)
        )
    }
}

class UserDetailEventBody(userDetail: UserDetailId) : DomainEventBody(
    mapOf(
        "userDetail" to userDetail
    )
) {
    fun getUserDetail(): UserDetailId {
        return toMap()["userDetail"] as UserDetailId
    }
}

enum class UserDetailEvent(val eventName: String) {
    CREATE("UserDetailCreate"),
}