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

package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import org.springframework.stereotype.Service

@Service
interface AccountService {
    suspend fun findById(id: Long): Account
    suspend fun findByIds(ids: List<Long>): List<Account>
}

@Service
class AccountServiceImpl(
    private val accountQueryService: AccountQueryService
) : AccountService {
    override suspend fun findById(id: Long): Account =
        accountQueryService.findById(id) ?: throw IllegalArgumentException("Account $id not found.")

    override suspend fun findByIds(ids: List<Long>): List<Account> = accountQueryService.findByIds(ids)
}
