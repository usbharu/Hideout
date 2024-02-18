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

package dev.usbharu.hideout.core.infrastructure.mongorepository

import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

@Suppress("LongParameterList", "FunctionMaxLength")
interface MongoTimelineRepository : MongoRepository<Timeline, Long> {
    fun findByUserId(id: Long): List<Timeline>
    fun findByUserIdAndTimelineId(userId: Long, timelineId: Long): List<Timeline>
    fun findByUserIdAndTimelineIdAndPostIdBetweenAndIsLocal(
        userId: Long?,
        timelineId: Long?,
        postIdMin: Long?,
        postIdMax: Long?,
        isLocal: Boolean?,
        pageable: Pageable
    ): List<Timeline>
}
