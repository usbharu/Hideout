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
import dev.usbharu.hideout.core.application.shared.UserDetailGettableCommandExecutor
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailRepository
import dev.usbharu.hideout.core.infrastructure.factory.PostContentFactoryImpl
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UpdateLocalNoteApplicationService(
    transaction: Transaction,
    private val postRepository: PostRepository,
    private val postContentFactoryImpl: PostContentFactoryImpl,
    private val userDetailRepository: UserDetailRepository,
    private val actorRepository: ActorRepository,
) : AbstractApplicationService<UpdateLocalNote, Unit>(transaction, logger) {

    override suspend fun internalExecute(command: UpdateLocalNote, executor: CommandExecutor) {
        require(executor is UserDetailGettableCommandExecutor)

        val userDetail = userDetailRepository.findById(executor.userDetailId)!!
        val actor = actorRepository.findById(userDetail.actorId)!!
        val post = postRepository.findById(PostId(command.postId))!!

        post.setContent(postContentFactoryImpl.create(command.content), actor)
        post.setOverview(command.overview?.let { PostOverview(it) }, actor)
        post.addMediaIds(command.mediaIds.map { MediaId(it) }, actor)
        post.setSensitive(command.sensitive, actor)

        postRepository.save(post)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(UpdateLocalNoteApplicationService::class.java)
    }
}