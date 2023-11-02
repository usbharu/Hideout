package dev.usbharu.hideout.activitypub.service.activity.delete

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Delete
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubResponse
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.query.PostQueryService
import io.ktor.http.*
import org.springframework.stereotype.Service

@Service
class APReceiveDeleteServiceImpl(
    private val postQueryService: PostQueryService,
    private val postRepository: PostRepository,
    private val transaction: Transaction
) : APReceiveDeleteService {
    override suspend fun receiveDelete(delete: Delete): ActivityPubResponse = transaction.transaction {
        val deleteId = delete.`object`?.id ?: throw IllegalActivityPubObjectException("object.id is null")

        val post = try {
            postQueryService.findByApId(deleteId)
        } catch (e: FailedToGetResourcesException) {
            return@transaction ActivityPubStringResponse(HttpStatusCode.OK, "Resource not found or already deleted")
        }
        postRepository.delete(post.id)
        return@transaction ActivityPubStringResponse(HttpStatusCode.OK, "Resource was deleted.")
    }
}
