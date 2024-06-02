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

import dev.usbharu.hideout.core.application.shared.Transaction
import dev.usbharu.hideout.core.domain.model.media.MediaId
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostOverview
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.infrastructure.factory.PostContentFactoryImpl
import org.springframework.stereotype.Service

@Service
class UpdateLocalNoteApplicationService(
    private val transaction: Transaction,
    private val postRepository: PostRepository,
    private val postContentFactoryImpl: PostContentFactoryImpl,
) {
    suspend fun update(updateLocalNote: UpdateLocalNote) {
        transaction.transaction {
            val post = postRepository.findById(PostId(updateLocalNote.postId))!!

            post.content = postContentFactoryImpl.create(updateLocalNote.content)
            post.overview = updateLocalNote.overview?.let { PostOverview(it) }
            post.addMediaIds(updateLocalNote.mediaIds.map { MediaId(it) })
            post.sensitive = updateLocalNote.sensitive

            postRepository.save(post)
        }
    }
}
