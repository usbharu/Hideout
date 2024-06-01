/*
 * Copyright (C) 2024 usbharu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.core.service.user

import dev.usbharu.hideout.activitypub.service.activity.delete.APSendDeleteService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.core.domain.model.deletedActor.DeletedActorRepository
import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.service.instance.InstanceService
import dev.usbharu.owl.producer.api.OwlProducer
import jakarta.validation.Validation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestApplicationConfig.testApplicationConfig
import java.net.URL
import java.security.KeyPairGenerator
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ActorServiceTest {

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var userAuthService: UserAuthService

    @Spy
    private val actorBuilder = Actor.UserBuilder(
        CharacterLimit(),
        ApplicationConfig(URL("https://example.com")),
        Validation.buildDefaultValidatorFactory().validator
    )

    @Spy
    private val applicationConfig: ApplicationConfig = testApplicationConfig.copy(private = false)

    @Mock
    private lateinit var instanceService: InstanceService

    @Mock
    private lateinit var userDetailRepository: UserDetailRepository

    @Mock
    private lateinit var deletedActorRepository: DeletedActorRepository

    @Mock
    private lateinit var reactionRepository: ReactionRepository

    @Mock
    private lateinit var relationshipRepository: RelationshipRepository

    @Mock
    private lateinit var postService: PostService

    @Mock
    private lateinit var apSendDeleteService: APSendDeleteService

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var owlProducer: OwlProducer

    @InjectMocks
    private lateinit var userService: UserServiceImpl

    @Test
    fun `createLocalUser ローカルユーザーを作成できる`() = runTest {

        val generateKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
        whenever(actorRepository.nextId()).doReturn(110001L)
        whenever(userAuthService.hash(anyString())).doReturn("hashedPassword")
        whenever(userAuthService.generateKeyPair()).doReturn(generateKeyPair)



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
    fun `createLocalUser applicationconfig privateがtrueのときアカウントを作成できない`() = runTest {
        whenever(applicationConfig.private).thenReturn(true)

        assertThrows<IllegalStateException> {
            userService.createLocalUser(UserCreateDto("test", "testUser", "XXXXXXXXXXXXX", "test"))
        }

    }

    @Test
    fun `createRemoteUser リモートユーザーを作成できる`() = runTest {

        whenever(actorRepository.nextId()).doReturn(113345L)
        whenever(instanceService.fetchInstance(eq("https://remote.example.com"), isNull())).doReturn(
            Instance(
                12345L,
                "",
                "",
                "https://remote.example.com",
                "https://remote.example.com/favicon.ico",
                null,
                "unknown",
                "",
                false,
                false,
                "",
                Instant.now()
            )
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
