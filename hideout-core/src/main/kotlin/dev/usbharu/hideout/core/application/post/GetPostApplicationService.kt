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

package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.shared.AbstractApplicationService
import dev.usbharu.hideout.core.application.shared.CommandExecutor
import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class GetPostApplicationService(private val postRepository: PostRepository, transaction: Transaction) :
    AbstractApplicationService<GetPost, Post>(transaction, logger) {

    override suspend fun internalExecute(command: GetPost, executor: CommandExecutor): Post {
        val post = postRepository.findById(PostId(command.postId)) ?: throw Exception("Post not found")

        return Post.of(post)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GetPostApplicationService::class.java)
    }
}