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

package dev.usbharu.hideout.mastodon.infrastructure.springweb

import dev.usbharu.hideout.domain.mastodon.model.generated.NotFoundResponse
import dev.usbharu.hideout.domain.mastodon.model.generated.UnprocessableEntityResponse
import dev.usbharu.hideout.domain.mastodon.model.generated.UnprocessableEntityResponseDetails
import dev.usbharu.hideout.mastodon.domain.exception.StatusNotFoundException
import dev.usbharu.hideout.mastodon.interfaces.api.account.MastodonAccountApiController
import dev.usbharu.hideout.mastodon.interfaces.api.apps.MastodonAppsApiController
import dev.usbharu.hideout.mastodon.interfaces.api.filter.MastodonFilterApiController
import dev.usbharu.hideout.mastodon.interfaces.api.instance.MastodonInstanceApiController
import dev.usbharu.hideout.mastodon.interfaces.api.media.MastodonMediaApiController
import dev.usbharu.hideout.mastodon.interfaces.api.notification.MastodonNotificationApiController
import dev.usbharu.hideout.mastodon.interfaces.api.status.MastodonStatusesApiContoller
import dev.usbharu.hideout.mastodon.interfaces.api.timeline.MastodonTimelineApiController
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice(
    assignableTypes = [
        MastodonAccountApiController::class,
        MastodonAppsApiController::class,
        MastodonFilterApiController::class,
        MastodonInstanceApiController::class,
        MastodonMediaApiController::class,
        MastodonNotificationApiController::class,
        MastodonStatusesApiContoller::class,
        MastodonTimelineApiController::class
    ]
)
class MastodonApiControllerAdvice {

    @ExceptionHandler(BindException::class)
    fun handleException(ex: BindException): ResponseEntity<UnprocessableEntityResponse> {
        logger.debug("Failed bind entity.", ex)
        val error = ex.bindingResult.fieldErrors
        val message = error.map {
            "${it.field} ${it.defaultMessage}"
        }.joinToString(prefix = "Validation failed: ")

        val details = error.associate {
            it.field to UnprocessableEntityResponseDetails(
                when (it.code) {
                    "Email" -> "ERR_INVALID"
                    "Pattern" -> "ERR_INVALID"
                    else -> "ERR_INVALID"
                },
                it.defaultMessage
            )
        }

        return ResponseEntity.unprocessableEntity().body(UnprocessableEntityResponse(message, details))
    }

    @ExceptionHandler(StatusNotFoundException::class)
    fun handleException(ex: StatusNotFoundException): ResponseEntity<NotFoundResponse> {
        logger.warn("Status not found.", ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getTypedResponse().response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(MastodonApiControllerAdvice::class.java)
    }
}