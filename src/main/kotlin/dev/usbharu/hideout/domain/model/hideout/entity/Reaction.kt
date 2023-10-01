package dev.usbharu.hideout.domain.model.hideout.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("REACTIONS")
data class Reaction(@Id val id: Long, val emojiId: Long, val postId: Long, val userId: Long)
