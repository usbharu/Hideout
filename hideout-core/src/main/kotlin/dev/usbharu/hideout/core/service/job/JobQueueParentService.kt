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

package dev.usbharu.hideout.core.service.job

import dev.usbharu.hideout.core.external.job.HideoutJob
import kjob.core.Job
import kjob.core.dsl.ScheduleContext
import org.springframework.stereotype.Service

@Service
interface JobQueueParentService {

    fun init(jobDefines: List<Job>)

    @Deprecated("use type safe → scheduleTypeSafe")
    suspend fun <J : Job> schedule(job: J, block: ScheduleContext<J>.(J) -> Unit = {})
    suspend fun <T, J : HideoutJob<T, J>> scheduleTypeSafe(job: J, jobProps: T)
}
