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

package dev.usbharu.hideout.mastodon.application.filter

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.filter.FilterKeywordId
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DeleteFilterV1ApplicationService(private val filterRepository: FilterRepository, transaction: Transaction) :
    AbstractApplicationService<DeleteFilterV1, Unit>(
        transaction, logger
    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteFilterV1ApplicationService::class.java)
    }

    override suspend fun internalExecute(command: DeleteFilterV1) {
        val filter = filterRepository.findByFilterKeywordId(FilterKeywordId(command.filterKeywordId))
            ?: throw Exception("Not Found")
        filterRepository.delete(filter)
    }
}