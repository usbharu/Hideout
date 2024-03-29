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

import kjob.core.job.JobProgress
import kjob.core.job.JobSettings
import kjob.core.job.JobStatus
import kjob.core.job.ScheduledJob
import kjob.core.repository.JobRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.Instant
import java.util.*

class ExposedJobRepository(
    private val database: Database,
    private val tableName: String,
    private val clock: Clock,
    private val json: Json
) :
    JobRepository {

    class Jobs(tableName: String) : LongIdTable(tableName) {
        val status = text("status")
        val runAt = long("runAt").nullable()
        val statusMessage = text("statusMessage").nullable()
        val retries = integer("retries")
        val kjobId = char("kjobId", 36).nullable()
        val createdAt = long("createdAt")
        val updatedAt = long("updatedAt")
        val jobId = text("jobId")
        val name = text("name")
        val properties = text("properties").nullable()
        val step = integer("step")
        val max = integer("max").nullable()
        val startedAt = long("startedAt").nullable()
        val completedAt = long("completedAt").nullable()
    }

    val jobs: Jobs = Jobs(tableName)

    fun createTable() {
        transaction(database) {
            SchemaUtils.create(jobs)
        }
    }

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun completeProgress(id: String): Boolean {
        val now = Instant.now(clock).toEpochMilli()
        return query {
            jobs.update({ jobs.id eq id.toLong() }) {
                it[jobs.completedAt] = now
                it[jobs.updatedAt] = now
            } == 1
        }
    }

    override suspend fun exist(jobId: String): Boolean {
        return query {
            jobs.selectAll().where(jobs.jobId eq jobId).empty().not()
        }
    }

    @Suppress("SuspendFunWithFlowReturnType")
    override suspend fun findNext(names: Set<String>, status: Set<JobStatus>, limit: Int): Flow<ScheduledJob> {
        return query {
            jobs.selectAll().where(
                jobs.status.inList(list = status.map { it.name })
                    .and(if (names.isEmpty()) Op.TRUE else jobs.name.inList(names))
            ).limit(limit)
                .map { it.toScheduledJob() }.asFlow()
        }
    }

    override suspend fun get(id: String): ScheduledJob? {
        val single = query { jobs.selectAll().where(jobs.id eq id.toLong()).singleOrNull() } ?: return null
        return single.toScheduledJob()
    }

    override suspend fun reset(id: String, oldKjobId: UUID?): Boolean {
        return query {
            jobs.update({
                jobs.id eq id.toLong() and if (oldKjobId == null) {
                    jobs.kjobId.isNull()
                } else {
                    jobs.kjobId eq oldKjobId.toString()
                }
            }) {
                it[jobs.status] = JobStatus.CREATED.name
                it[jobs.statusMessage] = null
                it[jobs.kjobId] = null
                it[jobs.step] = 0
                it[jobs.max] = null
                it[jobs.startedAt] = null
                it[jobs.completedAt] = null
                it[jobs.updatedAt] = Instant.now(clock).toEpochMilli()
            } == 1
        }
    }

    override suspend fun save(jobSettings: JobSettings, runAt: Instant?): ScheduledJob {
        val now = Instant.now(clock)
        val scheduledJob =
            ScheduledJob(
                id = "",
                status = JobStatus.CREATED,
                runAt = runAt,
                statusMessage = null,
                retries = 0,
                kjobId = null,
                createdAt = now,
                updatedAt = now,
                settings = jobSettings,
                progress = JobProgress(0)
            )
        val id = query {
            jobs.insert {
                it[jobs.status] = scheduledJob.status.name
                it[jobs.createdAt] = scheduledJob.createdAt.toEpochMilli()
                it[jobs.updatedAt] = scheduledJob.updatedAt.toEpochMilli()
                it[jobs.jobId] = scheduledJob.settings.id
                it[jobs.name] = scheduledJob.settings.name
                it[jobs.properties] = scheduledJob.settings.properties.stringify()
                it[jobs.runAt] = scheduledJob.runAt?.toEpochMilli()
                it[jobs.statusMessage] = null
                it[jobs.retries] = 0
                it[jobs.kjobId] = null
                it[jobs.step] = 0
                it[jobs.max] = null
                it[jobs.startedAt] = null
                it[jobs.completedAt] = null
            }[jobs.id].value
        }
        return scheduledJob.copy(id = id.toString())
    }

    override suspend fun setProgressMax(id: String, max: Long): Boolean {
        val now = Instant.now(clock).toEpochMilli()
        return query {
            jobs.update({ jobs.id eq id.toLong() }) {
                it[jobs.max] = max.toInt()
                it[jobs.updatedAt] = now
            } == 1
        }
    }

    override suspend fun startProgress(id: String): Boolean {
        val now = Instant.now(clock).toEpochMilli()
        return query {
            jobs.update({ jobs.id eq id.toLong() }) {
                it[jobs.startedAt] = now
                it[jobs.updatedAt] = now
            } == 1
        }
    }

    override suspend fun stepProgress(id: String, step: Long): Boolean {
        val now = Instant.now(clock).toEpochMilli()
        return query {
            jobs.update({ jobs.id eq id.toLong() }) {
                it[jobs.step] = jobs.step + step.toInt()
                it[jobs.updatedAt] = now
            } == 1
        }
    }

    override suspend fun update(
        id: String,
        oldKjobId: UUID?,
        kjobId: UUID?,
        status: JobStatus,
        statusMessage: String?,
        retries: Int
    ): Boolean {
        return query {
            jobs.update({
                (jobs.id eq id.toLong()) and if (oldKjobId == null) {
                    jobs.kjobId.isNull()
                } else {
                    jobs.kjobId eq oldKjobId.toString()
                }
            }) {
                it[jobs.status] = status.name
                it[jobs.retries] = retries
                it[jobs.updatedAt] = Instant.now(clock).toEpochMilli()
                it[jobs.id] = id.toLong()
                it[jobs.statusMessage] = statusMessage
                it[jobs.kjobId] = kjobId.toString()
            } == 1
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun String?.parseJsonMap(): Map<String, Any> {
        this ?: return emptyMap()
        return json.parseToJsonElement(this).jsonObject.mapValues { (_, el) ->
            if (el is JsonObject) {
                val t = el["t"]?.run { jsonPrimitive.content } ?: error("Cannot get jsonPrimitive")
                val value = el["v"]?.jsonArray ?: error("Cannot get jsonArray")
                when (t) {
                    "s" -> value.map { it.jsonPrimitive.content }
                    "d" -> value.map { it.jsonPrimitive.double }
                    "l" -> value.map { it.jsonPrimitive.long }
                    "i" -> value.map { it.jsonPrimitive.int }
                    "b" -> value.map { it.jsonPrimitive.boolean }
                    else -> error("Unknown type prefix '$t'")
                }.toList()
            } else {
                val content = el.jsonPrimitive.content
                val t = content.substringBefore(':')
                val value = content.substringAfter(':')
                when (t) {
                    "s" -> value
                    "d" -> value.toDouble()
                    "l" -> value.toLong()
                    "i" -> value.toInt()
                    "b" -> value.toBoolean()
                    else -> error("Unknown type prefix '$t'")
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun Map<String, Any>.stringify(): String? {
        if (isEmpty()) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        fun listSerialize(value: List<*>): JsonElement {
            return if (value.isEmpty()) {
                buildJsonObject {
                    put("t", "s")
                    putJsonArray("v") {}
                }
            } else {
                val (t, values) = when (val item = value.first()) {
                    is Double -> "d" to (value as List<Double>).map(::JsonPrimitive)
                    is Long -> "l" to (value as List<Long>).map(::JsonPrimitive)
                    is Int -> "i" to (value as List<Int>).map(::JsonPrimitive)
                    is String -> "s" to (value as List<String>).map(::JsonPrimitive)
                    is Boolean -> "b" to (value as List<Boolean>).map(::JsonPrimitive)
                    else -> error("Cannot serialize unsupported list property value: $item")
                }
                buildJsonObject {
                    put("t", t)
                    put("v", JsonArray(values))
                }
            }
        }

        fun createJsonPrimitive(string: String, value: Any) = JsonPrimitive("$string:$value")

        val jsonObject = JsonObject(
            mapValues { (_, value) ->
                when (value) {
                    is List<*> -> listSerialize(value)
                    is Double -> createJsonPrimitive("d", value)
                    is Long -> createJsonPrimitive("l", value)
                    is Int -> createJsonPrimitive("i", value)
                    is String -> createJsonPrimitive("s", value)
                    is Boolean -> createJsonPrimitive("b", value)
                    else -> error("Cannot serialize unsupported property value: $value")
                }
            }
        )
        return json.encodeToString(jsonObject)
    }

    private fun ResultRow.toScheduledJob(): ScheduledJob {
        val single = this

        return ScheduledJob(
            id = single[jobs.id].value.toString(),
            status = JobStatus.valueOf(single[jobs.status]),
            runAt = single[jobs.runAt]?.let { Instant.ofEpochMilli(it) },
            statusMessage = single[jobs.statusMessage],
            retries = single[jobs.retries],
            kjobId = single[jobs.kjobId]?.let {
                try {
                    @Suppress("SwallowedException")
                    UUID.fromString(it)
                } catch (ignored: IllegalArgumentException) {
                    null
                }
            },
            createdAt = Instant.ofEpochMilli(single[jobs.createdAt]),
            updatedAt = Instant.ofEpochMilli(single[jobs.updatedAt]),
            settings = JobSettings(
                id = single[jobs.jobId],
                name = single[jobs.name],
                properties = single[jobs.properties].parseJsonMap()
            ),
            progress = JobProgress(
                step = single[jobs.step].toLong(),
                max = single[jobs.max]?.toLong(),
                startedAt = single[jobs.startedAt]?.let { Instant.ofEpochMilli(it) },
                completedAt = single[jobs.completedAt]?.let { Instant.ofEpochMilli(it) }
            )
        )
    }
}
