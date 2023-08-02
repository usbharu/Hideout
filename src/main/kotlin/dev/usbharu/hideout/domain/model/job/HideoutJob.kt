package dev.usbharu.hideout.domain.model.job

import kjob.core.Job

sealed class HideoutJob(name: String = "") : Job(name)

object ReceiveFollowJob : HideoutJob("ReceiveFollowJob") {
    val actor = string("actor")
    val follow = string("follow")
    val targetActor = string("targetActor")
}

object DeliverPostJob : HideoutJob("DeliverPostJob") {
    val post = string("post")
    val actor = string("actor")
    val inbox = string("inbox")
}

object DeliverReactionJob : HideoutJob("DeliverReactionJob") {
    val reaction = string("reaction")
    val postUrl = string("postUrl")
    val actor = string("actor")
    val inbox = string("inbox")
    val id = string("id")
}

object DeliverRemoveReactionJob : HideoutJob("DeliverRemoveReactionJob") {
    val id = string("id")
    val inbox = string("inbox")
    val actor = string("actor")
    val like = string("like")
}
