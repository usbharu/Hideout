package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.interfaces.api.common.ActivityPubStringResponse
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.query.UserQueryService
import dev.usbharu.hideout.core.service.user.UserService
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import utils.TestTransaction
import utils.UserBuilder

class APAcceptServiceImplTest {

    @Test
    fun `receiveAccept 正常なAcceptを処理できる`() = runTest {
        val actor = "https://example.com"
        val follower = "https://follower.example.com"
        val targetUser = UserBuilder.localUserOf()
        val followerUser = UserBuilder.localUserOf()
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(eq(actor)) } doReturn targetUser
            onBlocking { findByUrl(eq(follower)) } doReturn followerUser
        }
        val followerQueryService = mock<FollowerQueryService> {
            onBlocking { alreadyFollow(eq(targetUser.id), eq(followerUser.id)) } doReturn false
        }
        val userService = mock<UserService>()
        val apAcceptServiceImpl =
            APAcceptServiceImpl(userService, userQueryService, followerQueryService, TestTransaction)

        val accept = Accept(
            name = "Accept",
            `object` = Follow(
                name = "",
                `object` = actor,
                actor = follower
            ),
            actor = actor
        )


        val actual = apAcceptServiceImpl.receiveAccept(accept)
        assertEquals(ActivityPubStringResponse(HttpStatusCode.OK, "accepted"), actual)
        verify(userService, times(1)).follow(eq(targetUser.id), eq(followerUser.id))
    }

    @Test
    fun `receiveAccept 既にフォローしている場合は無視する`() = runTest {

        val actor = "https://example.com"
        val follower = "https://follower.example.com"
        val targetUser = UserBuilder.localUserOf()
        val followerUser = UserBuilder.localUserOf()
        val userQueryService = mock<UserQueryService> {
            onBlocking { findByUrl(eq(actor)) } doReturn targetUser
            onBlocking { findByUrl(eq(follower)) } doReturn followerUser
        }
        val followerQueryService = mock<FollowerQueryService> {
            onBlocking { alreadyFollow(eq(targetUser.id), eq(followerUser.id)) } doReturn true
        }
        val userService = mock<UserService>()
        val apAcceptServiceImpl =
            APAcceptServiceImpl(userService, userQueryService, followerQueryService, TestTransaction)

        val accept = Accept(
            name = "Accept",
            `object` = Follow(
                name = "",
                `object` = actor,
                actor = follower
            ),
            actor = actor
        )


        val actual = apAcceptServiceImpl.receiveAccept(accept)
        assertEquals(ActivityPubStringResponse(HttpStatusCode.OK, "accepted"), actual)
        verify(userService, times(0)).follow(eq(targetUser.id), eq(followerUser.id))
    }

    @Test
    fun `revieveAccept AcceptのobjectのtypeがFollow以外の場合IllegalActivityPubObjectExceptionがthrowされる`() =
        runTest {
            val accept = Accept(
                name = "Accept",
                `object` = Like(
                    name = "Like",
                    actor = "actor",
                    id = "https://example.com",
                    `object` = "https://example.com",
                    content = "aaaa"
                ),
                actor = "https://example.com"
            )

            val apAcceptServiceImpl = APAcceptServiceImpl(mock(), mock(), mock(), TestTransaction)

            assertThrows<IllegalActivityPubObjectException> {
                apAcceptServiceImpl.receiveAccept(accept)
            }
        }
}
