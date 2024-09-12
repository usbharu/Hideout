package dev.usbharu.hideout.core.infrastructure.exposedrepository

import com.ninja_squad.dbsetup_kotlin.dbSetup
import dev.usbharu.hideout.core.domain.model.timeline.Timeline
import dev.usbharu.hideout.core.domain.model.timeline.TimelineId
import dev.usbharu.hideout.core.domain.model.timeline.TimelineName
import dev.usbharu.hideout.core.domain.model.timeline.TimelineVisibility
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import dev.usbharu.hideout.core.domain.shared.domainevent.DomainEventPublisher
import kotlinx.coroutines.test.runTest
import org.assertj.db.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import utils.AbstractRepositoryTest
import utils.columns
import utils.disableReferenceIntegrityConstraints
import utils.isEqualTo

@ExtendWith(MockitoExtension::class)
class ExposedTimelineRepositoryTest : AbstractRepositoryTest(Timelines) {
    @InjectMocks
    lateinit var repository: ExposedTimelineRepository

    @Mock
    lateinit var domainEventPublisher: DomainEventPublisher

    @Test
    fun save_idが同じレコードが存在しない場合insert() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PUBLIC,
            isSystem = false
        )

        repository.save(timeline)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Timelines.id, timeline.id.value)
            .isEqualTo(Timelines.userDetailId, timeline.userDetailId.id)
            .isEqualTo(Timelines.name, timeline.name.value)
            .isEqualTo(Timelines.visibility, timeline.visibility.name)
            .isEqualTo(Timelines.isSystem, timeline.isSystem)
    }

    @Test
    fun save_idが同じレコードが存在する場合update() = runTest {
        dbSetup(to = dataSource) {
            execute(disableReferenceIntegrityConstraints)
            insertInto(UserDetails.tableName) {
                columns(UserDetails.columns)
                values(1, 1, "veeeeeeeeeeeeeeryStrongPassword", true, null, null)
            }
            insertInto(Timelines.tableName) {
                columns(Timelines.columns)
                values(1, 1, "test-timeline", "PUBLIC", true)
            }

        }.launch()
        val timeline = Timeline(
            id = TimelineId(1),
            userDetailId = UserDetailId(1),
            name = TimelineName("timeline"),
            visibility = TimelineVisibility.PRIVATE,
            isSystem = false
        )

        repository.save(timeline)

        assertThat(assertTable)
            .row(0)
            .isEqualTo(Timelines.id, timeline.id.value)
            .isEqualTo(Timelines.userDetailId, timeline.userDetailId.id)
            .isEqualTo(Timelines.name, timeline.name.value)
            .isEqualTo(Timelines.visibility, timeline.visibility.name)
            .isEqualTo(Timelines.isSystem, timeline.isSystem)
    }
}
