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

package dev.usbharu.hideout.core.domain.exception.resource.local

import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import java.io.Serial

class LocalUserNotFoundException : UserNotFoundException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    )

    companion object {
        @Serial
        private const val serialVersionUID: Long = -4742548128672528145L

        fun withName(string: String, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("name: $string was not found.", throwable)

        fun withId(id: Long, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("id: $id was not found.", throwable)

        fun withUrl(url: String, throwable: Throwable? = null): LocalUserNotFoundException =
            LocalUserNotFoundException("url: $url was not found.", throwable)
    }
}
