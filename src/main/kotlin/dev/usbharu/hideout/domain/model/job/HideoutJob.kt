package dev.usbharu.hideout.domain.model.job

import kjob.core.Job
import kjob.core.Prop
import org.springframework.stereotype.Component

sealed class HideoutJob(name: String = "") : Job(name)

@Component
object ReceiveFollowJob : HideoutJob("ReceiveFollowJob") {
    val actor: Prop<ReceiveFollowJob, String> = string("actor")
    val follow: Prop<ReceiveFollowJob, String> = string("follow")
    val targetActor: Prop<ReceiveFollowJob, String> = string("targetActor")
}

@Component
object DeliverPostJob : HideoutJob("DeliverPostJob") {
    val post: Prop<DeliverPostJob, String> = string("post")
    val actor: Prop<DeliverPostJob, String> = string("actor")
    val inbox: Prop<DeliverPostJob, String> = string("inbox")
}

@Component
object DeliverReactionJob : HideoutJob("DeliverReactionJob") {
    val reaction: Prop<DeliverReactionJob, String> = string("reaction")
    val postUrl: Prop<DeliverReactionJob, String> = string("postUrl")
    val actor: Prop<DeliverReactionJob, String> = string("actor")
    val inbox: Prop<DeliverReactionJob, String> = string("inbox")
    val id: Prop<DeliverReactionJob, String> = string("id")
}

@Component
object DeliverRemoveReactionJob : HideoutJob("DeliverRemoveReactionJob") {
    val id: Prop<DeliverRemoveReactionJob, String> = string("id")
    val inbox: Prop<DeliverRemoveReactionJob, String> = string("inbox")
    val actor: Prop<DeliverRemoveReactionJob, String> = string("actor")
    val like: Prop<DeliverRemoveReactionJob, String> = string("like")
}
