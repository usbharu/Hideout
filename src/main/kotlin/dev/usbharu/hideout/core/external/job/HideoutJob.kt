package dev.usbharu.hideout.core.external.job

import kjob.core.Job
import kjob.core.Prop
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.stereotype.Component

abstract class HideoutJob<T, R : HideoutJob<T, R>>(name: String = "") : Job(name) {
    abstract fun convert(value: T): ScheduleContext<R>.(R) -> Unit
    abstract fun convert(props: JobProps<R>): T
}

@Component
object ReceiveFollowJob : HideoutJob<String, ReceiveFollowJob>("ReceiveFollowJob") {
    val actor: Prop<ReceiveFollowJob, String> = string("actor")
    val follow: Prop<ReceiveFollowJob, String> = string("follow")
    val targetActor: Prop<ReceiveFollowJob, String> = string("targetActor")

    override fun convert(value: String): ScheduleContext<ReceiveFollowJob>.(ReceiveFollowJob) -> Unit = {
        props[it.follow] = value
    }

    override fun convert(props: JobProps<ReceiveFollowJob>): String = TODO("Not yet implemented")
}

@Component
object DeliverPostJob : HideoutJob<String, DeliverPostJob>("DeliverPostJob") {
    val create = string("create")
    val inbox = string("inbox")
    val actor = string("actor")
    override fun convert(value: String): ScheduleContext<DeliverPostJob>.(DeliverPostJob) -> Unit {
        TODO("Not yet implemented")
    }

    override fun convert(props: JobProps<DeliverPostJob>): String {
        TODO("Not yet implemented")
    }
}

@Component
object DeliverReactionJob : HideoutJob<String, DeliverReactionJob>("DeliverReactionJob") {
    val reaction: Prop<DeliverReactionJob, String> = string("reaction")
    val postUrl: Prop<DeliverReactionJob, String> = string("postUrl")
    val actor: Prop<DeliverReactionJob, String> = string("actor")
    val inbox: Prop<DeliverReactionJob, String> = string("inbox")
    val id: Prop<DeliverReactionJob, String> = string("id")
}

@Component
object DeliverRemoveReactionJob : HideoutJob<String, DeliverRemoveReactionJob>("DeliverRemoveReactionJob") {
    val id: Prop<DeliverRemoveReactionJob, String> = string("id")
    val inbox: Prop<DeliverRemoveReactionJob, String> = string("inbox")
    val actor: Prop<DeliverRemoveReactionJob, String> = string("actor")
    val like: Prop<DeliverRemoveReactionJob, String> = string("like")
}

@Component
object InboxJob : HideoutJob<String, InboxJob>("InboxJob") {
    val json = string("json")
    val type = string("type")
    val httpRequest = string("http_request")
    val headers = string("headers")
}
