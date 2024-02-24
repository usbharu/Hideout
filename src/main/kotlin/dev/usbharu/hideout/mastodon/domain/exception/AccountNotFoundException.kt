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

import dev.usbharu.hideout.domain.mastodon.model.generated.NotFoundResponse
import dev.usbharu.hideout.mastodon.domain.model.MastodonApiErrorResponse

class AccountNotFoundException : ClientException {
    constructor(response: MastodonApiErrorResponse<NotFoundResponse>) : super(response)
    constructor(message: String?, response: MastodonApiErrorResponse<NotFoundResponse>) : super(message, response)
    constructor(message: String?, cause: Throwable?, response: MastodonApiErrorResponse<NotFoundResponse>) : super(
        message,
        cause,
        response
    )

    constructor(cause: Throwable?, response: MastodonApiErrorResponse<NotFoundResponse>) : super(cause, response)
    constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean,
        response: MastodonApiErrorResponse<NotFoundResponse>,
    ) : super(message, cause, enableSuppression, writableStackTrace, response)

    fun getTypedResponse(): MastodonApiErrorResponse<NotFoundResponse> =
        response as MastodonApiErrorResponse<NotFoundResponse>

    companion object {
        fun ofId(id: Long): AccountNotFoundException = AccountNotFoundException(
            "id: $id was not found.",
            MastodonApiErrorResponse(
                NotFoundResponse(
                    "Record not found"
                ),
                404
            ),
        )
    }
}