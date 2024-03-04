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

package dev.usbharu.owl.broker.service

import java.time.Instant
import kotlin.math.pow
import kotlin.math.roundToLong

interface RetryPolicy {
    fun nextRetry(now: Instant, attempt: Int): Instant
}

class ExponentialRetryPolicy(private val firstRetrySeconds: Int = 30) : RetryPolicy {
    override fun nextRetry(now: Instant, attempt: Int): Instant =
        now.plusSeconds(firstRetrySeconds.toDouble().pow(attempt + 1.0).roundToLong())

}