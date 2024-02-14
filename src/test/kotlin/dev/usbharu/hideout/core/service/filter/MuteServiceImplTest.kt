package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.*
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeywordRepository
import dev.usbharu.hideout.core.query.model.FilterQueryModel
import dev.usbharu.hideout.core.query.model.FilterQueryService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class MuteServiceImplTest {

    @Mock
    private lateinit var filterRepository: FilterRepository

    @Mock
    private lateinit var filterKeywordRepository: FilterKeywordRepository

    @Mock
    private lateinit var filterQueryService: FilterQueryService

    @InjectMocks
    private lateinit var muteServiceImpl: MuteServiceImpl

    @Test
    fun createFilter() = runTest {
        whenever(filterRepository.generateId()).doReturn(1)
        whenever(filterKeywordRepository.generateId()).doReturn(1)

        whenever(filterRepository.save(any())).doAnswer { it.arguments[0]!! as Filter }

        val createFilter = muteServiceImpl.createFilter(
            title = "hoge",
            context = listOf(FilterType.home, FilterType.public),
            action = FilterAction.warn,
            keywords = listOf(
                FilterKeyword(
                    "fuga",
                    FilterMode.NONE
                )
            ),
            loginUser = 1
        )

        assertThat(createFilter).isEqualTo(
            FilterQueryModel(
                1,
                1,
                "hoge",
                listOf(FilterType.home, FilterType.public),
                FilterAction.warn,
                keywords = listOf(
                    dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword(1, 1, "fuga", FilterMode.NONE)
                )
            )
        )
    }

    @Test
    fun getFilters() = runTest {
        whenever(filterQueryService.findByUserIdAndType(any(), any())).doReturn(
            listOf(
                FilterQueryModel(
                    1,
                    1,
                    "hoge",
                    listOf(FilterType.home),
                    FilterAction.warn,
                    listOf(
                        dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword(
                            1,
                            1,
                            "fuga",
                            FilterMode.NONE
                        )
                    )
                )
            )
        )

        muteServiceImpl.getFilters(1, listOf(FilterType.home))
    }

    @Test
    fun `getFilters 何も指定しない`() = runTest {
        whenever(filterQueryService.findByUserIdAndType(any(), eq(emptyList()))).doReturn(emptyList())

        muteServiceImpl.getFilters(1)
    }
}