package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.query.model.FilterQueryModel

interface MuteProcessService {
    suspend fun processMute(post: Post, context: List<FilterType>, filters: List<FilterQueryModel>): FilterResult?
    suspend fun processMutes(
        posts: List<Post>,
        context: List<FilterType>,
        filters: List<FilterQueryModel>
    ): Map<Post, FilterResult>
}
