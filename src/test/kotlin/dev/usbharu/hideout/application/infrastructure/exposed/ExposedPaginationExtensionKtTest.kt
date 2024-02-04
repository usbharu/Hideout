package dev.usbharu.hideout.application.infrastructure.exposed

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExposedPaginationExtensionKtTest {

    @BeforeEach
    fun setUp(): Unit = transaction {
        val map = (1..100).map { it to it.toString() }

        ExposePaginationTestTable.batchInsert(map){
            this[ExposePaginationTestTable.id] = it.first.toLong()
            this[ExposePaginationTestTable.name] = it.second
        }
    }

    @AfterEach
    fun tearDown():Unit = transaction {
        ExposePaginationTestTable.deleteAll()
    }

    @Test
    fun パラメーター無しでの取得(): Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(100)
        assertThat(pagination.prev).isEqualTo(81)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(100)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(81)
        assertThat(pagination).size().isEqualTo(20)
    }

    @Test
    fun maxIdを指定して取得(): Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(maxId = 100), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(99)
        assertThat(pagination.prev).isEqualTo(80)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(99)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(80)
        assertThat(pagination).size().isEqualTo(20)
    }

    @Test
    fun sinceIdを指定して取得(): Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(sinceId = 15), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(100)
        assertThat(pagination.prev).isEqualTo(81)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(100)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(81)
        assertThat(pagination).size().isEqualTo(20)
    }

    @Test
    fun minIdを指定して取得():Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(minId = 45), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(65)
        assertThat(pagination.prev).isEqualTo(46)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(65)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(46)
        assertThat(pagination).size().isEqualTo(20)
    }

    @Test
    fun maxIdとsinceIdを指定して取得(): Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(maxId = 45, sinceId = 34), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(44)
        assertThat(pagination.prev).isEqualTo(35)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(44)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(35)
        assertThat(pagination).size().isEqualTo(10)
    }

    @Test
    fun maxIdとminIdを指定して取得():Unit = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().limit(20).withPagination(Page.of(maxId = 54, minId = 45), ExposePaginationTestTable.id)

        assertThat(pagination.next).isEqualTo(53)
        assertThat(pagination.prev).isEqualTo(46)
        assertThat(pagination.first()[ExposePaginationTestTable.id]).isEqualTo(53)
        assertThat(pagination.last()[ExposePaginationTestTable.id]).isEqualTo(46)
        assertThat(pagination).size().isEqualTo(8)
    }

    @Test
    fun limitを指定して取得():Unit  = transaction {
        val pagination: PaginationList<ResultRow,Long> = ExposePaginationTestTable.selectAll().withPagination(Page.of(limit = 30), ExposePaginationTestTable.id)
        assertThat(pagination).size().isEqualTo(30)
    }

    @Test
    fun 結果が0件の場合はprevとnextがnullになる():Unit = transaction {
        val pagination = ExposePaginationTestTable.selectAll().where { ExposePaginationTestTable.id.isNull() }
            .withPagination(Page.of(), ExposePaginationTestTable.id)

        assertThat(pagination).isEmpty()
        assertThat(pagination.next).isNull()
        assertThat(pagination.prev).isNull()
    }

    object ExposePaginationTestTable : Table(){
        val id = long("id")
        val name = varchar("name",100)

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id)
    }

    companion object {
        private lateinit var database: Database

        @JvmStatic
        @BeforeAll
        fun beforeAll(): Unit {
            database = Database.connect(
                url = "jdbc:h2:mem:test;MODE=POSTGRESQL;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=true;TRACE_LEVEL_FILE=4;",
                driver = "org.h2.Driver",
                user = "sa",
                password = ""
            )

            transaction(database) {
                SchemaUtils.create(ExposePaginationTestTable)
                SchemaUtils.createMissingTablesAndColumns(ExposePaginationTestTable)
            }
        }
    }
}