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

package dev.usbharu.hideout.core.infrastructure.kjobexposed

import kjob.core.BaseKJob
import kjob.core.KJob
import kjob.core.KJobFactory
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import java.time.Clock

class ExposedKJob(config: Configuration) : BaseKJob<ExposedKJob.Configuration>(config) {

    private val database: Database = config.connectionDatabase ?: Database.connect(
        requireNotNull(config.connectionString),
        requireNotNull(config.driverClassName)
    )

    override val jobRepository: ExposedJobRepository
        get() = ExposedJobRepository(database, config.jobTableName, Clock.systemUTC(), config.json)

    override val lockRepository: ExposedLockRepository
        get() = ExposedLockRepository(database, config, clock)

    override fun start(): KJob {
        jobRepository.createTable()
        lockRepository.createTable()
        return super.start()
    }

    override fun shutdown(): Unit = runBlocking {
        super.shutdown()
        lockRepository.clearExpired()
    }

    companion object : KJobFactory<ExposedKJob, Configuration> {
        override fun create(configure: Configuration.() -> Unit): KJob = ExposedKJob(Configuration().apply(configure))
    }

    class Configuration : BaseKJob.Configuration() {
        var connectionString: String? = null
        var driverClassName: String? = null
        var connectionDatabase: Database? = null

        var jobTableName: String = "kjobJobs"

        var lockTableName: String = "kjobLocks"

        var expireLockInMinutes: Long = 5L
    }
}
