package dev.usbharu.hideout.service.api

import dev.usbharu.hideout.domain.model.ap.Note
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.query.FollowerQueryService
import dev.usbharu.hideout.query.activitypub.NoteQueryService
import dev.usbharu.hideout.service.core.Transaction
import org.springframework.stereotype.Service

@Service
class NoteApApiServiceImpl(
    private val noteQueryService: NoteQueryService,
    private val followerQueryService: FollowerQueryService,
    private val transaction: Transaction
) : NoteApApiService {
    override suspend fun getNote(postId: Long, userId: Long?): Note? = transaction.transaction {
        val findById = noteQueryService.findById(postId)
        when (findById.second.visibility) {
            Visibility.PUBLIC, Visibility.UNLISTED -> {
                return@transaction findById.first
            }

            Visibility.FOLLOWERS -> {
                if (userId == null) {
                    return@transaction null
                }

                if (followerQueryService.alreadyFollow(findById.second.userId, userId).not()) {
                    return@transaction null
                }
                return@transaction findById.first
            }

            Visibility.DIRECT -> return@transaction null
        }
    }
}
