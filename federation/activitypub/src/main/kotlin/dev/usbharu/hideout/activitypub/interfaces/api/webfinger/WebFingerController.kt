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

package dev.usbharu.hideout.activitypub.interfaces.api.webfinger

import dev.usbharu.hideout.activitypub.domain.model.webfinger.WebFinger
import dev.usbharu.hideout.activitypub.service.webfinger.WebFingerApiService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.resource.UserNotFoundException
import dev.usbharu.hideout.util.AcctUtil
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebFingerController(
    private val webFingerApiService: WebFingerApiService,
    private val applicationConfig: ApplicationConfig
) {
    @GetMapping("/.well-known/webfinger")
    fun webfinger(@RequestParam("resource") resource: String): ResponseEntity<WebFinger> = runBlocking {
        logger.info("WEBFINGER Lookup webfinger resource: {}", resource)
        val acct = try {
            AcctUtil.parse(resource.replace("acct:", ""))
        } catch (e: IllegalArgumentException) {
            logger.warn("FAILED Parse acct.", e)
            return@runBlocking ResponseEntity.badRequest().build()
        }
        val user = try {
            webFingerApiService.findByNameAndDomain(acct.username, acct.domain ?: applicationConfig.url.host)
        } catch (_: UserNotFoundException) {
            return@runBlocking ResponseEntity.notFound().build()
        }
        val webFinger = WebFinger(
            "acct:${user.name}@${user.domain}",
            listOf(
                WebFinger.Link(
                    "self",
                    "application/activity+json",
                    user.url
                )
            )
        )
        logger.info("SUCCESS Lookup webfinger resource: {} acct: {}", resource, acct)
        ResponseEntity(webFinger, HttpStatus.OK)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(WebFingerController::class.java)
    }
}
