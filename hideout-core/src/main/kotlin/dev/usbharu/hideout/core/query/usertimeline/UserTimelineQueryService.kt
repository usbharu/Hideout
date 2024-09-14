package dev.usbharu.hideout.core.query.usertimeline

import dev.usbharu.hideout.core.application.model.PostDetail
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.support.principal.Principal

interface UserTimelineQueryService {
    /**
     * replyやrepost等はnullになります
     */
    suspend fun findByIdAll(idList: List<PostId>, principal: Principal): List<PostDetail>
}
