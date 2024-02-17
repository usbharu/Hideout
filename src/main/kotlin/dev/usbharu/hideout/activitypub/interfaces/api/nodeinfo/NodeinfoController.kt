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

package dev.usbharu.hideout.activitypub.interfaces.api.nodeinfo

import dev.usbharu.hideout.activitypub.domain.model.nodeinfo.Nodeinfo
import dev.usbharu.hideout.activitypub.domain.model.nodeinfo.Nodeinfo2_0
import dev.usbharu.hideout.application.config.ApplicationConfig
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class NodeinfoController(private val applicationConfig: ApplicationConfig) {
    @GetMapping("/.well-known/nodeinfo")
    fun nodeinfo(): ResponseEntity<Nodeinfo> {
        return ResponseEntity(
            Nodeinfo(
                listOf(
                    Nodeinfo.Links(
                        "http://nodeinfo.diaspora.software/ns/schema/2.0",
                        "${applicationConfig.url}/nodeinfo/2.0"
                    )
                )
            ),
            HttpStatus.OK
        )
    }

    @GetMapping("/nodeinfo/2.0")
    @Suppress("FunctionNaming")
    fun nodeinfo2_0(): ResponseEntity<Nodeinfo2_0> {
        return ResponseEntity(
            Nodeinfo2_0(
                version = "2.0",
                software = Nodeinfo2_0.Software(
                    name = "hideout",
                    version = "0.0.1"
                ),
                protocols = listOf("activitypub"),
                services = Nodeinfo2_0.Services(
                    inbound = emptyList(),
                    outbound = emptyList()
                ),
                openRegistrations = false,
                usage = Nodeinfo2_0.Usage(
                    users = Nodeinfo2_0.Usage.Users(
                        total = 1,
                        activeHalfYear = 1,
                        activeMonth = 1
                    ),
                    localPosts = 1,
                    localComments = 0
                ),
                metadata = Nodeinfo2_0.Metadata(
                    nodeName = "hideout",
                    nodeDescription = "hideout test server",
                    maintainer = Nodeinfo2_0.Metadata.Maintainer("usbharu", "i@usbharu.dev"),
                    langs = emptyList(),
                    tosUrl = "",
                    repositoryUrl = "https://github.com/usbharu/Hideout",
                    feedbackUrl = "https://github.com/usbharu/Hideout/issues/new/choose",
                )
            ),
            HttpStatus.OK
        )
    }
}
