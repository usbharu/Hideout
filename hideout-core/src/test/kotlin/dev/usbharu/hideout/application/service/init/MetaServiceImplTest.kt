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

import dev.usbharu.hideout.core.domain.exception.NotInitException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import utils.TestTransaction
import java.util.*
import kotlin.test.assertEquals

class MetaServiceImplTest {
    @Test
    fun `getMeta メタデータを取得できる`() = runTest {
        val meta = Meta("1.0.0", Jwt(UUID.randomUUID(), "sdfsdjk", "adafda"))
        val metaRepository = mock<MetaRepository> {
            onBlocking { get() } doReturn meta
        }
        val metaService = MetaServiceImpl(metaRepository, TestTransaction)
        val actual = metaService.getMeta()
        assertEquals(meta, actual)
    }

    @Test
    fun `getMeta メタデータが無いときはNotInitExceptionがthrowされる`() = runTest {
        val metaRepository = mock<MetaRepository> {
            onBlocking { get() } doReturn null
        }
        val metaService = MetaServiceImpl(metaRepository, TestTransaction)
        assertThrows<NotInitException> { metaService.getMeta() }
    }

    @Test
    fun `updateMeta メタデータを保存できる`() = runTest {
        val meta = Meta("1.0.1", Jwt(UUID.randomUUID(), "sdfsdjk", "adafda"))
        val metaRepository = mock<MetaRepository> {
            onBlocking { save(any()) } doReturn Unit
        }
        val metaServiceImpl = MetaServiceImpl(metaRepository, TestTransaction)
        metaServiceImpl.updateMeta(meta)
        argumentCaptor<Meta> {
            verify(metaRepository).save(capture())
            assertEquals(meta, firstValue)
        }
    }

    @Test
    fun `getJwtMeta Jwtメタデータを取得できる`() = runTest {
        val meta = Meta("1.0.0", Jwt(UUID.randomUUID(), "sdfsdjk", "adafda"))
        val metaRepository = mock<MetaRepository> {
            onBlocking { get() } doReturn meta
        }
        val metaService = MetaServiceImpl(metaRepository, TestTransaction)
        val actual = metaService.getJwtMeta()
        assertEquals(meta.jwt, actual)
    }

    @Test
    fun `getJwtMeta メタデータが無いときはNotInitExceptionがthrowされる`() = runTest {
        val metaRepository = mock<MetaRepository> {
            onBlocking { get() } doReturn null
        }
        val metaService = MetaServiceImpl(metaRepository, TestTransaction)
        assertThrows<NotInitException> { metaService.getJwtMeta() }
    }
}
