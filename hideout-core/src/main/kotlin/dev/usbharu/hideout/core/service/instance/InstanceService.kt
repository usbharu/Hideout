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

package dev.usbharu.hideout.core.service.instance

import com.fasterxml.jackson.databind.ObjectMapper
import dev.usbharu.hideout.core.domain.model.instance.Instance
import dev.usbharu.hideout.core.domain.model.instance.InstanceRepository
import dev.usbharu.hideout.core.domain.model.instance.Nodeinfo
import dev.usbharu.hideout.core.domain.model.instance.Nodeinfo2_0
import dev.usbharu.hideout.core.service.resource.ResourceResolveService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Instant

interface InstanceService {
    suspend fun fetchInstance(url: String, sharedInbox: String? = null): Instance
    suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance
}

@Service
class InstanceServiceImpl(
    private val instanceRepository: InstanceRepository,
    private val resourceResolveService: ResourceResolveService,
    @Qualifier("activitypub") private val objectMapper: ObjectMapper,
) : InstanceService {
    override suspend fun fetchInstance(url: String, sharedInbox: String?): Instance {
        val u = URL(url)
        val resolveInstanceUrl = u.protocol + "://" + u.host

        val instance = instanceRepository.findByUrl(resolveInstanceUrl)

        if (instance != null) {
            return instance
        }

        logger.info("Instance not found. try fetch instance info. url: {}", resolveInstanceUrl)
        @Suppress("TooGenericExceptionCaught")
        try {
            val nodeinfoJson = resourceResolveService.resolve("$resolveInstanceUrl/.well-known/nodeinfo").bodyAsText()
            val nodeinfo = objectMapper.readValue(nodeinfoJson, Nodeinfo::class.java)
            val nodeinfoPathMap = nodeinfo.links.associate { it.rel to it.href }

            for ((key, value) in nodeinfoPathMap) {
                when (key) {
                    "http://nodeinfo.diaspora.software/ns/schema/2.0",
                    "http://nodeinfo.diaspora.software/ns/schema/2.1",
                    -> {
                        val nodeinfo20 = objectMapper.readValue(
                            resourceResolveService.resolve(value!!).bodyAsText(),
                            Nodeinfo2_0::class.java
                        )

                        val instanceCreateDto = InstanceCreateDto(
                            name = nodeinfo20.metadata?.nodeName,
                            description = nodeinfo20.metadata?.nodeDescription,
                            url = resolveInstanceUrl,
                            iconUrl = "$resolveInstanceUrl/favicon.ico",
                            sharedInbox = sharedInbox,
                            software = nodeinfo20.software?.name,
                            version = nodeinfo20.software?.version
                        )
                        return createNewInstance(instanceCreateDto)
                    }

                    else -> {
                        throw IllegalStateException("Unknown nodeinfo versions: $key url: $value")
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("FAILED Fetch Instance", e)
        }
        return createNewInstance(
            InstanceCreateDto(
                name = null,
                description = null,
                url = resolveInstanceUrl,
                iconUrl = "$resolveInstanceUrl/favicon.ico",
                sharedInbox = null,
                software = null,
                version = null
            )
        )
    }

    override suspend fun createNewInstance(instanceCreateDto: InstanceCreateDto): Instance {
        val instance = Instance(
            id = instanceRepository.generateId(),
            name = instanceCreateDto.name ?: instanceCreateDto.url,
            description = instanceCreateDto.description.orEmpty(),
            url = instanceCreateDto.url,
            iconUrl = instanceCreateDto.iconUrl,
            sharedInbox = instanceCreateDto.sharedInbox,
            software = instanceCreateDto.software ?: "unknown",
            version = instanceCreateDto.version ?: "unknown",
            isBlocked = false,
            isMuted = false,
            moderationNote = "",
            createdAt = Instant.now()
        )
        instanceRepository.save(instance)
        return instance
    }

    companion object {
        private val logger = LoggerFactory.getLogger(InstanceServiceImpl::class.java)
    }
}
