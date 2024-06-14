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

package dev.usbharu.hideout.core.application.media

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.CommandExecutor
import dev.usbharu.hideout.core.application.shared.Transaction
import org.slf4j.LoggerFactory

class UploadMediaApplicationService(transaction: Transaction) : AbstractApplicationService<UploadMedia, Media>(
    transaction, logger
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UploadMediaApplicationService::class.java)
    }

    override suspend fun internalExecute(command: UploadMedia, executor: CommandExecutor): Media {
        TODO()
    }
}