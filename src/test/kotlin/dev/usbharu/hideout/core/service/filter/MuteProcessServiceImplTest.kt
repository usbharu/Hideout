package dev.usbharu.hideout.core.service.filter

import dev.usbharu.hideout.core.domain.model.filter.FilterAction
import dev.usbharu.hideout.core.domain.model.filter.FilterMode
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.filterkeyword.FilterKeyword
import dev.usbharu.hideout.core.query.model.FilterQueryModel
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.PostBuilder

class MuteProcessServiceImplTest {
    @Test
    fun 単純な文字列にマッチする() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mute test")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "mute",
                    FilterMode.NONE
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isEqualTo(FilterResult(filterQueryModel, "mute"))
    }

    @Test
    fun 複数の文字列でマッチする() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mute test")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "mate",
                    FilterMode.NONE
                ),
                FilterKeyword(
                    1,
                    1,
                    "mata",
                    FilterMode.NONE
                ),
                FilterKeyword(
                    1,
                    1,
                    "mute",
                    FilterMode.NONE
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isEqualTo(FilterResult(filterQueryModel, "mute"))
    }

    @Test
    fun 単語にマッチする() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mute test")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "mute",
                    FilterMode.WHOLE_WORD
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isEqualTo(FilterResult(filterQueryModel, "mute"))
    }

    @Test
    fun 単語以外にはマッチしない() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mutetest")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "mute",
                    FilterMode.WHOLE_WORD
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isNull()
    }

    @Test
    fun 複数の単語にマッチする() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mute test")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "mate",
                    FilterMode.WHOLE_WORD
                ),
                FilterKeyword(
                    1,
                    1,
                    "mata",
                    FilterMode.WHOLE_WORD
                ),
                FilterKeyword(
                    1,
                    1,
                    "mute",
                    FilterMode.WHOLE_WORD
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isEqualTo(FilterResult(filterQueryModel, "mute"))
    }

    @Test
    fun 正規表現も使える() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(text = "mute test")

        val filterQueryModel = FilterQueryModel(
            1,
            2,
            "mute test",
            FilterType.entries,
            FilterAction.warn,
            listOf(
                FilterKeyword(
                    1,
                    1,
                    "e\\st",
                    FilterMode.REGEX
                )
            )
        )
        val actual = muteProcessServiceImpl.processMute(
            post, FilterType.entries.toList(), listOf(
                filterQueryModel
            )
        )

        assertThat(actual).isEqualTo(FilterResult(filterQueryModel, "e t"))
    }
}