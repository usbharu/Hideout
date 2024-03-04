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

package dev.usbharu.owl.broker.external

import com.google.protobuf.Timestamp
import dev.usbharu.owl.Uuid
import java.time.Instant
import java.util.*

fun Uuid.UUID.toUUID(): UUID = UUID(mostSignificantUuidBits, leastSignificantUuidBits)

fun UUID.toUUID(): Uuid.UUID = Uuid
    .UUID
    .newBuilder()
    .setMostSignificantUuidBits(mostSignificantBits)
    .setLeastSignificantUuidBits(leastSignificantBits)
    .build()

fun Timestamp.toInstant(): Instant = Instant.ofEpochSecond(seconds, nanos.toLong())

fun Instant.toTimestamp():Timestamp = Timestamp.newBuilder().setSeconds(this.epochSecond).setNanos(this.nano).build()