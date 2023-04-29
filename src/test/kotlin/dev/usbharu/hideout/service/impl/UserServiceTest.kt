@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.service.impl

import dev.usbharu.hideout.config.Config
import dev.usbharu.hideout.config.ConfigData
import dev.usbharu.hideout.domain.model.hideout.dto.RemoteUserCreateDto
import dev.usbharu.hideout.domain.model.hideout.dto.UserCreateDto
import dev.usbharu.hideout.repository.IUserRepository
import dev.usbharu.hideout.service.IUserAuthService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.*
import java.security.KeyPairGenerator
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserServiceTest {
    @Test
    fun `createLocalUser ローカルユーザーを作成できる`() = runTest {
        Config.configData = ConfigData(domain = "example.com", url = "https://example.com")
        val userRepository = mock<IUserRepository> {
            onBlocking { nextId() } doReturn 110001L
        }
        val generateKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
        val userAuthService = mock<IUserAuthService> {
            onBlocking { hash(anyString()) } doReturn "hashedPassword"
            onBlocking { generateKeyPair() } doReturn generateKeyPair
        }
        val userService = UserService(userRepository, userAuthService)
        userService.createLocalUser(UserCreateDto("test", "testUser", "XXXXXXXXXXXXX", "test"))
        verify(userRepository, times(1)).save(any())
        argumentCaptor<dev.usbharu.hideout.domain.model.hideout.entity.User> {
            verify(userRepository, times(1)).save(capture())
            assertEquals("test", firstValue.name)
            assertEquals("testUser", firstValue.screenName)
            assertEquals("XXXXXXXXXXXXX", firstValue.description)
            assertEquals("hashedPassword", firstValue.password)
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

        Config.configData = ConfigData(domain = "example.com", url = "https://example.com")


        val userRepository = mock<IUserRepository> {
            onBlocking { nextId() } doReturn 113345L
        }
        val userService = UserService(userRepository, mock())
        val user = RemoteUserCreateDto(
            "test",
            "example.com",
            "testUser",
            "test user",
            "https://example.com/inbox",
            "https://example.com/outbox",
            "https://example.com",
            "-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----"
        )
        userService.createRemoteUser(user)
        verify(userRepository, times(1)).save(any())
        argumentCaptor<dev.usbharu.hideout.domain.model.hideout.entity.User> {
            verify(userRepository, times(1)).save(capture())
            assertEquals("test", firstValue.name)
            assertEquals("testUser", firstValue.screenName)
            assertEquals("test user", firstValue.description)
            assertNull(firstValue.password)
            assertEquals(113345L, firstValue.id)
            assertEquals("https://example.com", firstValue.url)
            assertEquals("example.com", firstValue.domain)
            assertEquals("https://example.com/inbox", firstValue.inbox)
            assertEquals("https://example.com/outbox", firstValue.outbox)
            assertEquals("-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----", firstValue.publicKey)
            assertNull(firstValue.privateKey)
        }
    }
}
