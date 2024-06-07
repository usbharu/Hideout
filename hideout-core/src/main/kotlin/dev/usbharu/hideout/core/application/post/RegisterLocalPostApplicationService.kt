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
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.factory.PostFactoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RegisterLocalPostApplicationService(
    private val postFactory: PostFactoryImpl,
    private val actorRepository: ActorRepository,
    private val postRepository: PostRepository,
    private val userDetailRepository: UserDetailRepository,
    transaction: Transaction,
) : AbstractApplicationService<RegisterLocalPost, Unit>(transaction, Companion.logger) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RegisterLocalPostApplicationService::class.java)
    }

    override suspend fun internalExecute(command: RegisterLocalPost, executor: CommandExecutor) {
        val actorId = (userDetailRepository.findById(command.userDetailId)
            ?: throw IllegalStateException("actor not found")).actorId

        val post = postFactory.createLocal(actorId,
            actorRepository.findById(actorId)!!.name,
            command.overview?.let { PostOverview(it) },
            command.content,
            command.visibility,
            command.repostId?.let { PostId(it) },
            command.replyId?.let { PostId(it) },
            command.sensitive,
            command.mediaIds.map { MediaId(it) })

        postRepository.save(post)
    }
}
