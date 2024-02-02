package dev.usbharu.hideout.activitypub.domain.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import dev.usbharu.hideout.activitypub.domain.model.objects.Object

open class Announce @JsonCreator constructor(
    type: List<String> = emptyList(),
    @JsonProperty("object")
    val apObject: String,
    override val actor: String,
    override val id: String,
    val published: String,
    val to: List<String> = emptyList(),
    val cc: List<String> = emptyList()
) : Object(
    type = add(type, "Announce")
), HasActor, HasId