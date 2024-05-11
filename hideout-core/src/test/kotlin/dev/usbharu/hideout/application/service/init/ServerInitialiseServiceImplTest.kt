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

package dev.usbharu.hideout.application.service.init

import dev.usbharu.hideout.core.domain.model.meta.Jwt
import dev.usbharu.hideout.core.domain.model.meta.Meta
import dev.usbharu.hideout.core.domain.model.meta.MetaRepository
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
        val metaRepository = mock<MetaRepository> {
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
        val metaRepository = mock<MetaRepository> {
            onBlocking { get() } doReturn meta
        }
        val serverInitialiseServiceImpl = ServerInitialiseServiceImpl(metaRepository, TestTransaction)
        serverInitialiseServiceImpl.init()
        verify(metaRepository, times(0)).save(any())
    }

    @Test
    fun `init メタデータが存在して違うバージョンのときはバージョンを変更する`() = runTest {
        val meta = Meta("1.0.0", Jwt(UUID.randomUUID(), "aaafafd", "afafasdf"))
        val metaRepository = mock<MetaRepository> {
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
