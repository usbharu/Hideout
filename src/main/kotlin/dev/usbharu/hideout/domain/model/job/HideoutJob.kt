package dev.usbharu.hideout.domain.model.job

import kjob.core.Job

sealed class HideoutJob(name: String = "") : Job(name)

object ReceiveFollowJob : HideoutJob("ReceiveFollowJob"){
    val actor = string("actor")
    val follow = string("follow")
    val targetActor = string("targetActor")
}
