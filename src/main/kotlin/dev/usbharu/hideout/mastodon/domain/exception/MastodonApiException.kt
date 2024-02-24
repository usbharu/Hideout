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

package dev.usbharu.hideout.mastodon.domain.exception

import dev.usbharu.hideout.mastodon.domain.model.MastodonApiErrorResponse

abstract class MastodonApiException : RuntimeException {

    val response: MastodonApiErrorResponse<*>

    constructor(response: MastodonApiErrorResponse<*>) : super() {
        this.response = response
    }

    constructor(message: String?, response: MastodonApiErrorResponse<*>) : super(message) {
        this.response = response
    }

    constructor(message: String?, cause: Throwable?, response: MastodonApiErrorResponse<*>) : super(message, cause) {
        this.response = response
    }

    constructor(cause: Throwable?, response: MastodonApiErrorResponse<*>) : super(cause) {
        this.response = response
    }

    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        response: MastodonApiErrorResponse<*>,
    ) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace
    ) {
        this.response = response
    }
}