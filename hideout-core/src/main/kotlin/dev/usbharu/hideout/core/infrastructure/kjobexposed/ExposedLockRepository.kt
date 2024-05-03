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

import kjob.core.job.Lock
import kjob.core.repository.LockRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes

class ExposedLockRepository(
    private val database: Database,
    private val config: ExposedKJob.Configuration,
    private val clock: Clock
) : LockRepository {

    class Locks(tableName: String) : UUIDTable(tableName) {
        val updatedAt = long("updatedAt")
        val expiresAt = long("expiresAt")
    }

    val locks: Locks = Locks(config.lockTableName)

    fun createTable() {
        transaction(database) {
            SchemaUtils.create(locks)
        }
    }

    @Suppress("InjectDispatcher")
    suspend fun <T> query(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun exists(id: UUID): Boolean {
        val now = Instant.now(clock)
        return query {
            locks.selectAll().where(locks.id eq id and locks.expiresAt.greater(now.toEpochMilli())).empty().not()
        }
    }

    override suspend fun ping(id: UUID): Lock {
        val now = Instant.now(clock)
        val expiresAt = now.plusSeconds(config.expireLockInMinutes.minutes.inWholeSeconds)
        val lock = Lock(id, now)
        query {
            if (locks.selectAll().where(locks.id eq id).limit(1)
                    .map { Lock(it[locks.id].value, Instant.ofEpochMilli(it[locks.expiresAt])) }.isEmpty()
            ) {
                locks.insert {
                    it[locks.id] = id
                    it[locks.updatedAt] = now.toEpochMilli()
                    it[locks.expiresAt] = expiresAt.toEpochMilli()
                }
            } else {
                locks.update({ locks.id eq id }) {
                    it[locks.updatedAt] = now.toEpochMilli()
                    it[locks.expiresAt] = expiresAt.toEpochMilli()
                }
            }
        }
        return lock
    }

    suspend fun clearExpired() {
        val now = Instant.now(clock).toEpochMilli()
        query {
            locks.deleteWhere { locks.expiresAt greater now }
        }
    }
}
