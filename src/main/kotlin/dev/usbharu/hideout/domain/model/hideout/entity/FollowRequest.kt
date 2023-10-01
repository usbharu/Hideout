package dev.usbharu.hideout.domain.model.hideout.entity

import org.springframework.data.relational.core.mapping.Table

@Table("follow_requests")
data class FollowRequest(val userId: Long, val followerId: Long)
