package dev.usbharu.hideout.core.domain.model.followtimeline

import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

class FollowTimeline(val userDetailId: UserDetailId, val timelineId: TimelineId)
