/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.usbharu.hideout.core.external.job

import dev.usbharu.hideout.activitypub.service.common.ActivityType
import kjob.core.Job
import kjob.core.Prop
import kjob.core.dsl.ScheduleContext
import kjob.core.job.JobProps
import org.springframework.stereotype.Component

abstract class HideoutJob<out T, out R : HideoutJob<T, R>>(name: String) : Job(name) {
    abstract fun convert(value: @UnsafeVariance T): ScheduleContext<@UnsafeVariance R>.(@UnsafeVariance R) -> Unit
    fun convertUnsafe(props: JobProps<*>): T = convert(props as JobProps<R>)
    abstract fun convert(props: JobProps<@UnsafeVariance R>): T
}

data class ReceiveFollowJobParam(
    val actor: String,
    val follow: String,
    val targetActor: String
)

@Component
object ReceiveFollowJob : HideoutJob<ReceiveFollowJobParam, ReceiveFollowJob>("ReceiveFollowJob") {
    val actor: Prop<ReceiveFollowJob, String> = string("actor")
    val follow: Prop<ReceiveFollowJob, String> = string("follow")
    val targetActor: Prop<ReceiveFollowJob, String> = string("targetActor")

    override fun convert(value: ReceiveFollowJobParam): ScheduleContext<ReceiveFollowJob>.(ReceiveFollowJob) -> Unit = {
        props[follow] = value.follow
        props[actor] = value.actor
        props[targetActor] = value.targetActor
    }

    override fun convert(props: JobProps<ReceiveFollowJob>): ReceiveFollowJobParam = ReceiveFollowJobParam(
        actor = props[actor],
        follow = props[follow],
        targetActor = props[targetActor]
    )
}

data class DeliverPostJobParam(
    val create: String,
    val inbox: String,
    val actor: String
)

@Component
object DeliverPostJob : HideoutJob<DeliverPostJobParam, DeliverPostJob>("DeliverPostJob") {
    val create = string("create")
    val inbox = string("inbox")
    val actor = string("actor")
    override fun convert(value: DeliverPostJobParam): ScheduleContext<DeliverPostJob>.(DeliverPostJob) -> Unit = {
        props[create] = value.create
        props[inbox] = value.inbox
        props[actor] = value.actor
    }

    override fun convert(props: JobProps<DeliverPostJob>): DeliverPostJobParam = DeliverPostJobParam(
        create = props[create],
        inbox = props[inbox],
        actor = props[actor]
    )
}

data class DeliverReactionJobParam(
    val reaction: String,
    val postUrl: String,
    val actor: String,
    val inbox: String,
    val id: String
)

@Component
object DeliverReactionJob : HideoutJob<DeliverReactionJobParam, DeliverReactionJob>("DeliverReactionJob") {
    val reaction: Prop<DeliverReactionJob, String> = string("reaction")
    val postUrl: Prop<DeliverReactionJob, String> = string("postUrl")
    val actor: Prop<DeliverReactionJob, String> = string("actor")
    val inbox: Prop<DeliverReactionJob, String> = string("inbox")
    val id: Prop<DeliverReactionJob, String> = string("id")
    override fun convert(
        value: DeliverReactionJobParam
    ): ScheduleContext<DeliverReactionJob>.(DeliverReactionJob) -> Unit =
        {
            props[reaction] = value.reaction
            props[postUrl] = value.postUrl
            props[actor] = value.actor
            props[inbox] = value.inbox
            props[id] = value.id
        }

    override fun convert(props: JobProps<DeliverReactionJob>): DeliverReactionJobParam = DeliverReactionJobParam(
        props[reaction],
        props[postUrl],
        props[actor],
        props[inbox],
        props[id]
    )
}

data class DeliverRemoveReactionJobParam(
    val id: String,
    val inbox: String,
    val actor: String,
    val like: String
)

@Component
object DeliverRemoveReactionJob :
    HideoutJob<DeliverRemoveReactionJobParam, DeliverRemoveReactionJob>("DeliverRemoveReactionJob") {
    val id: Prop<DeliverRemoveReactionJob, String> = string("id")
    val inbox: Prop<DeliverRemoveReactionJob, String> = string("inbox")
    val actor: Prop<DeliverRemoveReactionJob, String> = string("actor")
    val like: Prop<DeliverRemoveReactionJob, String> = string("like")

    override fun convert(
        value: DeliverRemoveReactionJobParam
    ): ScheduleContext<DeliverRemoveReactionJob>.(DeliverRemoveReactionJob) -> Unit =
        {
            props[id] = value.id
            props[inbox] = value.inbox
            props[actor] = value.actor
            props[like] = value.like
        }

    override fun convert(props: JobProps<DeliverRemoveReactionJob>): DeliverRemoveReactionJobParam =
        DeliverRemoveReactionJobParam(
            id = props[id],
            inbox = props[inbox],
            actor = props[actor],
            like = props[like]
        )
}

data class InboxJobParam(
    val json: String,
    val type: ActivityType,
    val httpRequest: String,
    val headers: String
)

@Component
object InboxJob : HideoutJob<InboxJobParam, InboxJob>("InboxJob") {
    val json = string("json")
    val type = string("type")
    val httpRequest = string("http_request")
    val headers = string("headers")

    override fun convert(value: InboxJobParam): ScheduleContext<InboxJob>.(InboxJob) -> Unit = {
        props[json] = value.json
        props[type] = value.type.name
        props[httpRequest] = value.httpRequest
        props[headers] = value.headers
    }

    override fun convert(props: JobProps<InboxJob>): InboxJobParam = InboxJobParam(
        props[json],
        ActivityType.valueOf(props[type]),
        props[httpRequest],
        props[headers]
    )
}
