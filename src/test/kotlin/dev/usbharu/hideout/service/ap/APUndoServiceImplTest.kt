package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.domain.model.ActivityPubStringResponse
import dev.usbharu.hideout.domain.model.ap.Follow
import dev.usbharu.hideout.domain.model.ap.Undo
import dev.usbharu.hideout.query.UserQueryService
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import utils.TestTransaction
import utils.UserBuilder
import java.time.Instant

class APUndoServiceImplTest {
    @Test
    fun `receiveUndo FollowのUndoを処理できる`() = runTest {

        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(eq("https://follower.example.com/actor")) } doReturn UserBuilder.remoteUserOf()
            onBlocking { findByUrl(eq("https://example.com/actor")) } doReturn UserBuilder.localUserOf()
        }
        val apUndoServiceImpl = APUndoServiceImpl(
            userService = mock(),
            apUserService = mock(),
            userQueryService = userQueryService,
            transaction = TestTransaction
        )

        val undo = Undo(
            name = "Undo",
            actor = "https://follower.example.com/actor",
            id = "https://follower.example.com/undo/follow",
            `object` = Follow(
                name = "Follow",
                `object` = "https://example.com/actor",
                actor = "https://follower.example.com/actor"
            ),
            published = Instant.now()
        )
        val activityPubResponse = apUndoServiceImpl.receiveUndo(undo)
        assertEquals(ActivityPubStringResponse(HttpStatusCode.OK, "Accept"), activityPubResponse)
    }

}
