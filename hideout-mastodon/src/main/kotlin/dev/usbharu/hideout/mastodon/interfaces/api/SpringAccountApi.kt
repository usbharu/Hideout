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

package dev.usbharu.hideout.mastodon.interfaces.api

import dev.usbharu.hideout.mastodon.interfaces.api.generated.AccountApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.*
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringAccountApi : AccountApi {
    override suspend fun apiV1AccountsIdBlockPost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdBlockPost(id)
    }

    override suspend fun apiV1AccountsIdFollowPost(
        id: String,
        followRequestBody: FollowRequestBody?,
    ): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdFollowPost(id, followRequestBody)
    }

    override suspend fun apiV1AccountsIdGet(id: String): ResponseEntity<Account> {
        return super.apiV1AccountsIdGet(id)
    }

    override suspend fun apiV1AccountsIdMutePost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdMutePost(id)
    }

    override suspend fun apiV1AccountsIdRemoveFromFollowersPost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdRemoveFromFollowersPost(id)
    }

    override suspend fun apiV1AccountsIdUnblockPost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdUnblockPost(id)
    }

    override suspend fun apiV1AccountsIdUnfollowPost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdUnfollowPost(id)
    }

    override suspend fun apiV1AccountsIdUnmutePost(id: String): ResponseEntity<Relationship> {
        return super.apiV1AccountsIdUnmutePost(id)
    }

    override suspend fun apiV1AccountsPost(accountsCreateRequest: AccountsCreateRequest): ResponseEntity<Unit> {
        return super.apiV1AccountsPost(accountsCreateRequest)
    }

    override suspend fun apiV1AccountsUpdateCredentialsPatch(updateCredentials: UpdateCredentials?): ResponseEntity<Account> {
        return super.apiV1AccountsUpdateCredentialsPatch(updateCredentials)
    }

    override suspend fun apiV1AccountsVerifyCredentialsGet(): ResponseEntity<CredentialAccount> {
        return super.apiV1AccountsVerifyCredentialsGet()
    }

    override suspend fun apiV1FollowRequestsAccountIdAuthorizePost(accountId: String): ResponseEntity<Relationship> {
        return super.apiV1FollowRequestsAccountIdAuthorizePost(accountId)
    }

    override suspend fun apiV1FollowRequestsAccountIdRejectPost(accountId: String): ResponseEntity<Relationship> {
        return super.apiV1FollowRequestsAccountIdRejectPost(accountId)
    }

}