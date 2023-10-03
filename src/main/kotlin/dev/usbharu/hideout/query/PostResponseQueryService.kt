package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.dto.PostResponse
import org.springframework.stereotype.Repository

@Suppress("LongParameterList")
@Repository
interface PostResponseQueryService {
    suspend fun findById(id: Long, userId: Long?): PostResponse

}
