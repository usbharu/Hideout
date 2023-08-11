@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.JwtRefreshToken
import dev.usbharu.hideout.service.core.IdGenerateService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class JwtRefreshTokenRepositoryImplTest {

    lateinit var db: Database

    @BeforeEach
    fun setUp() {
        db = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        transaction(db) {
            SchemaUtils.create(JwtRefreshTokens)
        }
    }

    @AfterEach
    fun tearDown() {
        transaction(db) {
            SchemaUtils.drop(JwtRefreshTokens)
        }
    }

    @Test
    fun `save 存在しない場合はinsertする`() = runTest {
        val repository = JwtRefreshTokenRepositoryImpl(
            db,
            object : IdGenerateService {
                override suspend fun generateId(): Long {
                    TODO("Not yet implemented")
                }
            }
        )
        val now = Instant.now(Clock.tickMillis(ZoneId.systemDefault()))
        val expiresAt = now.plus(10, ChronoUnit.MINUTES)

        val expect = JwtRefreshToken(1L, 2L, "refreshToken", now, expiresAt)
        newSuspendedTransaction {
            repository.save(expect)
            val actual = repository.findById(1L)
            assertEquals(expect, actual)
        }
    }

    @Test
    fun `save 存在する場合はupdateする`() = runTest {
        val repository = JwtRefreshTokenRepositoryImpl(
            db,
            object : IdGenerateService {
                override suspend fun generateId(): Long {
                    TODO("Not yet implemented")
                }
            }
        )
        newSuspendedTransaction {
            JwtRefreshTokens.insert {
                it[id] = 1L
                it[userId] = 2L
                it[refreshToken] = "refreshToken1"
                it[createdAt] = Instant.now().toEpochMilli()
                it[expiresAt] = Instant.now().plus(10, ChronoUnit.MINUTES).toEpochMilli()
            }
            repository.save(
                JwtRefreshToken(
                    id = 1L,
                    userId = 2L,
                    refreshToken = "refreshToken2",
                    createdAt = Instant.now(),
                    expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES)
                )
            )
        }

        transaction {
            val toJwtRefreshToken = JwtRefreshTokens.select { JwtRefreshTokens.id.eq(1L) }.single().toJwtRefreshToken()
            assertEquals("refreshToken2", toJwtRefreshToken.refreshToken)
        }
    }
}
