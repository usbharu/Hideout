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

package dev.usbharu.hideout.activitypub.service.activity.follow

import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.core.external.job.ReceiveFollowTask
import dev.usbharu.owl.producer.api.OwlProducer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

interface APReceiveFollowService {
    suspend fun receiveFollow(follow: Follow)
}

@Service
class APReceiveFollowServiceImpl(
    private val owlProducer: OwlProducer,
) : APReceiveFollowService {
    override suspend fun receiveFollow(follow: Follow) {
        logger.info("FOLLOW from: {} to: {}", follow.actor, follow.apObject)
        owlProducer.publishTask(ReceiveFollowTask(follow.actor, follow, follow.apObject))
        return
    }

    companion object {
        private val logger = LoggerFactory.getLogger(APReceiveFollowServiceImpl::class.java)
    }
}
