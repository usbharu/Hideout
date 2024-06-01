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

package dev.usbharu.hideout.core.service.notification

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.notification.Notification
import dev.usbharu.hideout.core.domain.model.notification.NotificationRepository
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.UserBuilder
import java.net.URL

@ExtendWith(MockitoExtension::class)
class NotificationServiceImplTest {


    @Mock
    private lateinit var relationshipNotificationManagementService: RelationshipNotificationManagementService

    @Mock
    private lateinit var relationshipRepository: RelationshipRepository

    @Spy
    private val notificationStoreList: MutableList<NotificationStore> = mutableListOf()

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var postRepository: PostRepository

    @Mock
    private lateinit var reactionRepository: ReactionRepository

    @Spy
    private val applicationConfig = ApplicationConfig(URL("https://example.com"))

    @InjectMocks
    private lateinit var notificationServiceImpl: NotificationServiceImpl

    @Test
    fun `publishNotifi ローカルユーザーへの通知を発行する`() = runTest {

        val actor = UserBuilder.localUserOf(domain = "example.com")

        whenever(actorRepository.findById(eq(1))).doReturn(actor)

        val id = TwitterSnowflakeIdGenerateService.generateId()

        whenever(notificationRepository.generateId()).doReturn(id)

        whenever(notificationRepository.save(any())).doAnswer { it.arguments[0] as Notification }


        val actual = notificationServiceImpl.publishNotify(PostNotificationRequest(1, 2, 3))

        assertThat(actual).isNotNull()

        verify(notificationRepository, times(1)).save(any())
    }

    @Test
    fun `publishNotify ユーザーが存在しないときは発行しない`() = runTest {
        val actual = notificationServiceImpl.publishNotify(PostNotificationRequest(1, 2, 3))

        assertThat(actual).isNull()
    }

    @Test
    fun `publishNotify ユーザーがリモートユーザーの場合は発行しない`() = runTest {
        val actor = UserBuilder.remoteUserOf(domain = "remote.example.com")

        whenever(actorRepository.findById(eq(1))).doReturn(actor)

        val actual = notificationServiceImpl.publishNotify(PostNotificationRequest(1, 2, 3))

        assertThat(actual).isNull()
    }

    @Test
    fun unpublishNotify() = runTest {
        notificationServiceImpl.unpublishNotify(1)
    }
}
