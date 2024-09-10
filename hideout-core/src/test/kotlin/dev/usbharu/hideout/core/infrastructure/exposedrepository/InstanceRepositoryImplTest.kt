package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.model.instance.*
import dev.usbharu.hideout.core.domain.model.instance.Instance
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import utils.AbstractRepositoryTest
import utils.isEqualTo
import utils.value
import java.net.URI
import java.sql.Timestamp
import java.time.Instant
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Instance as InstanceTable

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstanceRepositoryImplTest : AbstractRepositoryTest(InstanceTable) {


    @Test
    fun save() = runTest {
        InstanceRepositoryImpl().save(
            Instance(
                id = InstanceId(1),
                name = InstanceName("test"),
                description = InstanceDescription("id"),
                url = URI.create("https://www.example.com"),
                iconUrl = URI.create("https://www.example.com"),
                sharedInbox = null,
                software = InstanceSoftware(""),
                version = InstanceVersion(""),
                isBlocked = false,
                isMuted = false,
                moderationNote = InstanceModerationNote(""),
                createdAt = Instant.parse("2020-01-01T00:00:00Z"),
            )
        )

        val table = assertTable
        assertThat(table)
            .row(1)
            .isEqualTo(InstanceTable.id, 1)
            .isEqualTo(InstanceTable.name, "test")
            .isEqualTo(InstanceTable.url, "https://www.example.com")
            .isEqualTo(InstanceTable.iconUrl, "https://www.example.com")
            .isEqualTo(InstanceTable.sharedInbox, null)
            .isEqualTo(InstanceTable.software, "")
            .isEqualTo(InstanceTable.version, "")
            .isEqualTo(InstanceTable.isBlocked, false)
            .isEqualTo(InstanceTable.isMuted, false)
            .isEqualTo(InstanceTable.moderationNote, "")
            .value(InstanceTable.createdAt).isEqualTo(Timestamp.from(Instant.parse("2020-01-01T00:00:00Z")))
    }
}

