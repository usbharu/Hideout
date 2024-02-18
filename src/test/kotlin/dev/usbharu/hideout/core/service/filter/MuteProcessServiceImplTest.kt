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

    @Test
    fun cw文字にマッチする() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(overview = "mute test", text = "hello")

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

    @Test
    fun 文字列と単語と正規表現を同時に使える() = runTest {
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
                ),
                FilterKeyword(
                    2,
                    1,
                    "mute",
                    FilterMode.NONE
                ),
                FilterKeyword(
                    3,
                    1,
                    "test",
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
    fun 複数の投稿を処理できる() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()


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
        val posts = listOf(
            PostBuilder.of(text = "mute"), PostBuilder.of(text = "mutes"), PostBuilder.of(text = "hoge")
        )
        val actual = muteProcessServiceImpl.processMutes(
            posts,
            FilterType.entries.toList(),
            listOf(
                filterQueryModel
            )
        )

        assertThat(actual)
            .hasSize(2)
            .containsEntry(posts[0], FilterResult(filterQueryModel, "mute"))
            .containsEntry(posts[1], FilterResult(filterQueryModel, "mute"))

    }

    @Test
    fun 何もマッチしないとnullが返ってくる() = runTest {
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
                    "fuga",
                    FilterMode.NONE
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
    fun Cwで何もマッチしないと本文を確認する() = runTest {
        val muteProcessServiceImpl = MuteProcessServiceImpl()

        val post = PostBuilder.of(overview = "hage", text = "mute test")

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
                    "fuga",
                    FilterMode.NONE
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

}