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

import dev.usbharu.hideout.core.domain.model.actor.Actor2Repository
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.Post2Repository
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.infrastructure.factory.PostFactoryImpl
import org.springframework.stereotype.Service

@Service
class RegisterLocalPostApplicationService(
    private val postFactory: PostFactoryImpl,
    private val actor2Repository: Actor2Repository,
    private val postRepository: Post2Repository,
) {
    suspend fun register(registerLocalPost: RegisterLocalPost) {

        val actorId = ActorId(registerLocalPost.actorId)
        val post = postFactory.createLocal(
            actorId,
            actor2Repository.findById(actorId)!!.name,
            PostOverview(registerLocalPost.overview),
            registerLocalPost.content,
            registerLocalPost.visibility,
            registerLocalPost.repostId?.let { PostId(it) },
            registerLocalPost.replyId?.let { PostId(it) },
            registerLocalPost.sensitive,
            registerLocalPost.mediaIds.map { MediaId(it) }
        )

        postRepository.save(post)
    }
}