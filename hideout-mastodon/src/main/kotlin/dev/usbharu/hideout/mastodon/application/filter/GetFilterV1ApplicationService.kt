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
import dev.usbharu.hideout.core.application.shared.CommandExecutor
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.filter.FilterContext.*
import dev.usbharu.hideout.core.domain.model.filter.FilterKeywordId
import dev.usbharu.hideout.core.domain.model.filter.FilterMode
import dev.usbharu.hideout.core.domain.model.filter.FilterRepository
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.V1Filter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class GetFilterV1ApplicationService(private val filterRepository: FilterRepository, transaction: Transaction) :
    AbstractApplicationService<GetFilterV1, V1Filter>(
        transaction, logger
    ) {
    override suspend fun internalExecute(command: GetFilterV1, executor: CommandExecutor): V1Filter {
        val filter = filterRepository.findByFilterKeywordId(FilterKeywordId(command.filterKeywordId))
            ?: throw Exception("Not Found")

        val filterKeyword = filter.filterKeywords.find { it.id.id == command.filterKeywordId }
        return V1Filter(
            id = filter.id.id.toString(),
            phrase = filterKeyword?.keyword?.keyword,
            context = filter.filterContext.map {
                when (it) {
                    home -> V1Filter.Context.home
                    notifications -> V1Filter.Context.notifications
                    public -> V1Filter.Context.public
                    thread -> V1Filter.Context.thread
                    account -> V1Filter.Context.account
                }
            },
            expiresAt = null,
            irreversible = false,
            wholeWord = filterKeyword?.mode == FilterMode.WHOLE_WORD
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetFilterV1ApplicationService::class.java)
    }
}