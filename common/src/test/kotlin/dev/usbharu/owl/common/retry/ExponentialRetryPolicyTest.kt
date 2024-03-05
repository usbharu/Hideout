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

package dev.usbharu.owl.common.retry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class ExponentialRetryPolicyTest {
    @Test
    fun exponential0() {
        val nextRetry = ExponentialRetryPolicy().nextRetry(Instant.ofEpochSecond(300), 0)

        assertEquals(Instant.ofEpochSecond(330), nextRetry)
    }

    @Test
    fun exponential1() {
        val nextRetry = ExponentialRetryPolicy().nextRetry(Instant.ofEpochSecond(300), 1)
        assertEquals(Instant.ofEpochSecond(360), nextRetry)
    }
}