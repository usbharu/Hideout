@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.service.core

import dev.usbharu.hideout.domain.model.hideout.entity.Jwt
import dev.usbharu.hideout.domain.model.hideout.entity.Meta
import dev.usbharu.hideout.repository.IMetaRepository
import dev.usbharu.hideout.util.ServerUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import utils.TestTransaction
import java.util.*
import kotlin.test.assertEquals

class ServerInitialiseServiceImplTest {
    @Test
    fun `init メタデータが無いときに初期化を実行する`() = runTest {
        val metaRepository = mock<IMetaRepository> {
            onBlocking { get() } doReturn null
            onBlocking { save(any()) } doReturn Unit
        }
        val serverInitialiseServiceImpl = ServerInitialiseServiceImpl(metaRepository, TestTransaction)

        serverInitialiseServiceImpl.init()
        verify(metaRepository, times(1)).save(any())
    }

    @Test
    fun `init メタデータが存在して同じバージョンのときは何もしない`() = runTest {
        val meta = Meta(ServerUtil.getImplementationVersion(), Jwt(UUID.randomUUID(), "aaafafd", "afafasdf"))
        val metaRepository = mock<IMetaRepository> {
            onBlocking { get() } doReturn meta
        }
        val serverInitialiseServiceImpl = ServerInitialiseServiceImpl(metaRepository, TestTransaction)
        serverInitialiseServiceImpl.init()
        verify(metaRepository, times(0)).save(any())
    }

    @Test
    fun `init メタデータが存在して違うバージョンのときはバージョンを変更する`() = runTest {
        val meta = Meta("1.0.0", Jwt(UUID.randomUUID(), "aaafafd", "afafasdf"))
        val metaRepository = mock<IMetaRepository> {
            onBlocking { get() } doReturn meta
            onBlocking { save(any()) } doReturn Unit
        }

        val serverInitialiseServiceImpl = ServerInitialiseServiceImpl(metaRepository, TestTransaction)
        serverInitialiseServiceImpl.init()
        verify(metaRepository, times(1)).save(any())
        argumentCaptor<Meta> {
            verify(metaRepository, times(1)).save(capture())
            assertEquals(ServerUtil.getImplementationVersion(), firstValue.version)
        }
    }
}
