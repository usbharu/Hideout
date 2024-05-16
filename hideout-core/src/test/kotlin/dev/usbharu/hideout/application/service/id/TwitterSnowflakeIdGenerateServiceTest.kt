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

package dev.usbharu.hideout.application.service.id

// import kotlinx.coroutines.NonCancellable.message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwitterSnowflakeIdGenerateServiceTest {
    @Test
    fun noDuplicateTest() = runBlocking {
        val mutex = Mutex()
        val mutableListOf = mutableListOf<Long>()
        coroutineScope {
            repeat(500000) {
                launch(Dispatchers.IO) {
                    val id = TwitterSnowflakeIdGenerateService.generateId()
                    mutex.withLock {
                        mutableListOf.add(id)
                    }
                }
            }
        }

        assertEquals(0, mutableListOf.size - mutableListOf.toSet().size)
    }
}
