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

package dev.usbharu.hideout.core.service.post

import dev.usbharu.hideout.core.domain.model.post.Visibility

data class PostCreateDto(
    val text: String,
    val overview: String? = null,
    val visibility: Visibility = Visibility.PUBLIC,
    val repostId: Long? = null,
    val repolyId: Long? = null,
    val userId: Long,
    val mediaIds: List<Long> = emptyList()
)
