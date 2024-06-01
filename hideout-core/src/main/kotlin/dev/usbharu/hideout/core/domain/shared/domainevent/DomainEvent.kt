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

package dev.usbharu.hideout.core.domain.shared.domainevent

import java.time.Instant
import java.util.*

/**
 * エンティティで発生したドメインイベント
 *
 * @property id ID
 * @property name ドメインイベント名
 * @property occurredOn 発生時刻
 * @property body ドメインイベントのボディ
 * @property collectable trueで同じドメインイベント名でをまとめる
 */
data class DomainEvent(
    val id: String,
    val name: String,
    val occurredOn: Instant,
    val body: DomainEventBody,
    val collectable: Boolean = false
) {
    companion object {
        fun create(name: String, body: DomainEventBody, collectable: Boolean = false): DomainEvent {
            return DomainEvent(UUID.randomUUID().toString(), name, Instant.now(), body, collectable)
        }

        fun reconstruct(
            id: String,
            name: String,
            occurredOn: Instant,
            body: DomainEventBody,
            collectable: Boolean
        ): DomainEvent {
            return DomainEvent(id, name, occurredOn, body, collectable)
        }
    }
}