package dev.usbharu.hideout.activitypub.service.`object`.note

import dev.usbharu.hideout.activitypub.domain.model.Note
import dev.usbharu.hideout.activitypub.query.NoteQueryService
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.query.FollowerQueryService
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
