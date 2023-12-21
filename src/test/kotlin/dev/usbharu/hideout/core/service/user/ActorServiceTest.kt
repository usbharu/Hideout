@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.actor.Actor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.post.Post
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import utils.TestApplicationConfig.testApplicationConfig
import java.net.URL
import java.security.KeyPairGenerator
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActorServiceTest {
    val actorBuilder = Actor.UserBuilder(CharacterLimit(), ApplicationConfig(URL("https://example.com")))
    val postBuilder = Post.PostBuilder(CharacterLimit())
    @Test
    fun `createLocalUser ローカルユーザーを作成できる`() = runTest {

        val actorRepository = mock<ActorRepository> {
            onBlocking { nextId() } doReturn 110001L
        }
        val generateKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
        val userAuthService = mock<UserAuthService> {
            onBlocking { hash(anyString()) } doReturn "hashedPassword"
            onBlocking { generateKeyPair() } doReturn generateKeyPair
        }
        val userService =
            UserServiceImpl(
                actorRepository = actorRepository,
                userAuthService = userAuthService,
                actorBuilder = actorBuilder,
                applicationConfig = testApplicationConfig,
                instanceService = mock(),
                userDetailRepository = mock(),
                deletedActorRepository = mock(),
                reactionRepository = mock(),
                relationshipRepository = mock(),
                postService = mock(),
                apSendDeleteService = mock()
            )
        userService.createLocalUser(UserCreateDto("test", "testUser", "XXXXXXXXXXXXX", "test"))
        verify(actorRepository, times(1)).save(any())
        argumentCaptor<Actor> {
            verify(actorRepository, times(1)).save(capture())
            assertEquals("test", firstValue.name)
            assertEquals("testUser", firstValue.screenName)
            assertEquals("XXXXXXXXXXXXX", firstValue.description)
            assertEquals(110001L, firstValue.id)
            assertEquals("https://example.com/users/test", firstValue.url)
            assertEquals("example.com", firstValue.domain)
            assertEquals("https://example.com/users/test/inbox", firstValue.inbox)
            assertEquals("https://example.com/users/test/outbox", firstValue.outbox)
            assertEquals(generateKeyPair.public.toPem(), firstValue.publicKey)
            assertEquals(generateKeyPair.private.toPem(), firstValue.privateKey)
        }
    }

    @Test
    fun `createRemoteUser リモートユーザーを作成できる`() = runTest {

        val actorRepository = mock<ActorRepository> {
            onBlocking { nextId() } doReturn 113345L
        }
        val deletedActorQueryService = mock<DeletedActorQueryService> {
            onBlocking {
                findByNameAndDomain(
                    eq("test"),
                    eq("remote.example.com")
                )
            } doAnswer { throw FailedToGetResourcesException() }
        }
        val userService =
            UserServiceImpl(
                actorRepository = actorRepository,
                userAuthService = mock(),
                actorBuilder = actorBuilder,
                applicationConfig = testApplicationConfig,
                instanceService = mock(),
                userDetailRepository = mock(),
                deletedActorRepository = mock(),
                reactionRepository = mock(),
                relationshipRepository = mock(),
                postService = mock(),
                apSendDeleteService = mock()
            )
        val user = RemoteUserCreateDto(
            name = "test",
            domain = "remote.example.com",
            screenName = "testUser",
            description = "test user",
            inbox = "https://remote.example.com/inbox",
            outbox = "https://remote.example.com/outbox",
            url = "https://remote.example.com",
            publicKey = "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----",
            keyId = "a",
            following = "",
            followers = "",
            sharedInbox = null,
            locked = false
        )
        userService.createRemoteUser(user)
        verify(actorRepository, times(1)).save(any())
        argumentCaptor<Actor> {
            verify(actorRepository, times(1)).save(capture())
            assertEquals("test", firstValue.name)
            assertEquals("testUser", firstValue.screenName)
            assertEquals("test user", firstValue.description)
            assertEquals(113345L, firstValue.id)
            assertEquals("https://remote.example.com", firstValue.url)
            assertEquals("remote.example.com", firstValue.domain)
            assertEquals("https://remote.example.com/inbox", firstValue.inbox)
            assertEquals("https://remote.example.com/outbox", firstValue.outbox)
            assertEquals("-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----", firstValue.publicKey)
            assertNull(firstValue.privateKey)
        }
    }
}
