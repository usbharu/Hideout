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

package dev.usbharu.hideout.activitypub.service.common

import dev.usbharu.hideout.activitypub.domain.exception.ActivityPubProcessException
import dev.usbharu.hideout.activitypub.domain.exception.FailedProcessException
import dev.usbharu.hideout.activitypub.domain.exception.HttpSignatureUnauthorizedException
import dev.usbharu.hideout.activitypub.domain.model.objects.Object
import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.exception.resource.ResourceAccessException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.SQLException

abstract class AbstractActivityPubProcessor<T : Object>(
    private val transaction: Transaction,
    private val allowUnauthorized: Boolean = false
) : ActivityPubProcessor<T> {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun process(activity: ActivityPubProcessContext<T>) {
        if (activity.isAuthorized.not() && allowUnauthorized.not()) {
            throw HttpSignatureUnauthorizedException()
        }
        logger.info("START ActivityPub process. {}", this.type())
        try {
            transaction.transaction {
                try {
                    internalProcess(activity)
                } catch (e: ResourceAccessException) {
                    throw SQLException(e)
                }
            }
        } catch (e: ActivityPubProcessException) {
            logger.warn("FAILED ActivityPub process", e)
            throw FailedProcessException("Failed process", e)
        }
        logger.info("SUCCESS ActivityPub process. {}", this.type())
    }

    abstract suspend fun internalProcess(activity: ActivityPubProcessContext<T>)
}
