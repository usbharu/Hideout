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

import dev.usbharu.hideout.core.application.post.RegisterLocalPost
import dev.usbharu.hideout.core.application.post.RegisterLocalPostApplicationService
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.infrastructure.springframework.oauth2.Oauth2CommandExecutorFactory
import dev.usbharu.hideout.mastodon.interfaces.api.generated.StatusApi
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.Status
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.StatusesRequest
import dev.usbharu.hideout.mastodon.interfaces.api.generated.model.StatusesRequest.Visibility.*
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

@Controller
class SpringStatusApi(
    private val oauth2CommandExecutorFactory: Oauth2CommandExecutorFactory,
    private val registerLocalPostApplicationService: RegisterLocalPostApplicationService,
) : StatusApi {
    override suspend fun apiV1StatusesIdEmojiReactionsEmojiDelete(id: String, emoji: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdEmojiReactionsEmojiDelete(id, emoji)
    }

    override suspend fun apiV1StatusesIdEmojiReactionsEmojiPut(id: String, emoji: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdEmojiReactionsEmojiPut(id, emoji)
    }

    override suspend fun apiV1StatusesIdGet(id: String): ResponseEntity<Status> {
        return super.apiV1StatusesIdGet(id)
    }

    override suspend fun apiV1StatusesPost(statusesRequest: StatusesRequest): ResponseEntity<Status> {
        val executor = oauth2CommandExecutorFactory.getCommandExecutor()
        registerLocalPostApplicationService.execute(
            RegisterLocalPost(
                userDetailId = executor.userDetailId,
                content = statusesRequest.status.orEmpty(),
                overview = statusesRequest.spoilerText,
                visibility = when (statusesRequest.visibility) {
                    public -> Visibility.PUBLIC
                    unlisted -> Visibility.UNLISTED
                    private -> Visibility.FOLLOWERS
                    direct -> Visibility.DIRECT
                    null -> Visibility.PUBLIC
                },
                repostId = null,
                replyId = statusesRequest.inReplyToId?.toLong(),
                sensitive = statusesRequest.sensitive == true,
                mediaIds = statusesRequest.mediaIds.orEmpty().map { it.toLong() }
            ),
            executor
        )
        return ResponseEntity.ok().build()
    }
}