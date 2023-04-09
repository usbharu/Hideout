package dev.usbharu.hideout.domain.model.job

import kjob.core.Job

object ReceiveFollowJob : Job("ReceiveFollowJob"){
    val actor = string("actor")
    val follow = string("follow")
    val targetActor = string("targetActor")
}
