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

package dev.usbharu.hideout.mastodon.interfaces.api.apps

import dev.usbharu.hideout.controller.mastodon.generated.AppApi
import dev.usbharu.hideout.domain.mastodon.model.generated.Application
import dev.usbharu.hideout.domain.mastodon.model.generated.AppsRequest
import dev.usbharu.hideout.mastodon.service.app.AppApiService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

@Controller
class MastodonAppsApiController(private val appApiService: AppApiService) : AppApi {
    override suspend fun apiV1AppsPost(appsRequest: AppsRequest): ResponseEntity<Application> {
        return ResponseEntity(
            appApiService.createApp(appsRequest),
            HttpStatus.OK
        )
    }

    @RequestMapping(
        method = [RequestMethod.POST],
        value = ["/api/v1/apps"],
        produces = ["application/json"],
        consumes = ["application/x-www-form-urlencoded"]
    )
    suspend fun apiV1AppsPost(@RequestParam map: Map<String, String>): ResponseEntity<Application> {
        val appsRequest =
            AppsRequest(map.getValue("client_name"), map.getValue("redirect_uris"), map["scopes"], map["website"])
        return ResponseEntity(
            appApiService.createApp(appsRequest),
            HttpStatus.OK
        )
    }
}
