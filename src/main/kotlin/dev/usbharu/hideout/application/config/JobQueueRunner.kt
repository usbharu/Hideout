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

package dev.usbharu.hideout.application.config

import dev.usbharu.hideout.core.external.job.HideoutJob
import dev.usbharu.hideout.core.service.job.JobQueueParentService
import dev.usbharu.hideout.core.service.job.JobQueueWorkerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class JobQueueRunner(
    private val jobQueueParentService: JobQueueParentService,
    private val jobs: List<HideoutJob<*, *>>
) :
    ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue. ${jobs.size}")
        jobQueueParentService.init(jobs)
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(JobQueueRunner::class.java)
    }
}

@Component
class JobQueueWorkerRunner(
    private val jobQueueWorkerService: JobQueueWorkerService,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        LOGGER.info("Init job queue worker.")
        jobQueueWorkerService.init<Any?, HideoutJob<*, *>>(emptyList())
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(JobQueueWorkerRunner::class.java)
    }
}
