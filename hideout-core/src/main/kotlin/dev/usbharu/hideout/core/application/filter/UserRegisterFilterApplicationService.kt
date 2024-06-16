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

package dev.usbharu.hideout.core.application.filter

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.CommandExecutor
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.application.shared.UserDetailGettableCommandExecutor
import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.filter.FilterKeyword
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.id.IdGenerateService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserRegisterFilterApplicationService(
    private val idGenerateService: IdGenerateService,
    private val filterRepository: FilterRepository,
    transaction: Transaction,
) :
    AbstractApplicationService<RegisterFilter, Filter>(
        transaction,
        logger
    ) {

    companion object {
        private val logger = LoggerFactory.getLogger(UserRegisterFilterApplicationService::class.java)
    }

    override suspend fun internalExecute(command: RegisterFilter, executor: CommandExecutor): Filter {
        require(executor is UserDetailGettableCommandExecutor)

        val filter = dev.usbharu.hideout.core.domain.model.filter.Filter.create(
            id = FilterId(idGenerateService.generateId()),
            userDetailId = UserDetailId(executor.userDetailId),
            name = FilterName(command.filterName),
            filterContext = command.filterContext,
            filterAction = command.filterAction,
            filterKeywords = command.filterKeywords
                .map {
                    FilterKeyword(
                        FilterKeywordId(idGenerateService.generateId()),
                        FilterKeywordKeyword(it.keyword),
                        it.filterMode
                    )
                }.toSet()
        )

        filterRepository.save(filter)
        return Filter.of(filter)
    }
}
