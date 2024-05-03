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

package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.service.resource.InMemoryCacheManager
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.UserBuilder
import dev.usbharu.hideout.activitypub.domain.model.objects.Object as APObject

@ExtendWith(MockitoExtension::class)

class APResourceResolveServiceImplTest {

    @Test
    fun `単純な一回のリクエスト`() = runTest {


        val actorRepository = mock<ActorRepository>()

        val user = UserBuilder.localUserOf()
        whenever(actorRepository.findById(any())) doReturn user

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, actorRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("https", 0)

        verify(apRequestService, times(1)).apGet(eq("https"), eq(user), eq(APObject::class.java))
    }

    @Test
    fun 複数回の同じリクエストが重複して発行されない() = runTest {


        val actorRepository = mock<ActorRepository>()

        val user = UserBuilder.localUserOf()
        whenever(actorRepository.findById(any())) doReturn user

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, actorRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)
        apResourceResolveService.resolve<APObject>("https", 0)

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(APObject::class.java)
        )
    }

    @Test
    fun 複数回の同じリクエストが同時に発行されても重複して発行されない() = runTest {


        val actorRepository = mock<ActorRepository>()
        val user = UserBuilder.localUserOf()

        whenever(actorRepository.findById(any())) doReturn user


        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    eq("https"),
                    eq(user),
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }
        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, actorRepository, InMemoryCacheManager())

        repeat(10) {
            awaitAll(
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
                async { apResourceResolveService.resolve<APObject>("https", 0) },
            )
        }

        verify(apRequestService, times(1)).apGet(
            eq("https"),
            eq(user),
            eq(APObject::class.java)
        )
    }

    @Test
    fun 関係のないリクエストは発行する() = runTest {

        val actorRepository = mock<ActorRepository>()

        val user = UserBuilder.localUserOf()
        whenever(actorRepository.findById(any())).doReturn(
            user
        )

        val apRequestService = mock<APRequestService> {
            onBlocking {
                apGet(
                    any(),
                    eq(user),
                    eq(APObject::class.java)
                )
            } doReturn APObject(
                emptyList()
            )
        }

        val apResourceResolveService =
            APResourceResolveServiceImpl(apRequestService, actorRepository, InMemoryCacheManager())

        apResourceResolveService.resolve<APObject>("abcd", 0)
        apResourceResolveService.resolve<APObject>("1234", 0)
        apResourceResolveService.resolve<APObject>("aaaa", 0)

        verify(apRequestService, times(3)).apGet(
            any(),
            eq(user),
            eq(APObject::class.java)
        )
    }


}
