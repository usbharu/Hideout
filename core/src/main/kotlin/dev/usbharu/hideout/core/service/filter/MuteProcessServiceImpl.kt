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

import dev.usbharu.hideout.core.domain.model.filter.FilterMode.*
import dev.usbharu.hideout.core.domain.model.filter.FilterType
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.query.model.FilterQueryModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MuteProcessServiceImpl : MuteProcessService {
    override suspend fun processMute(
        post: Post,
        context: List<FilterType>,
        filters: List<FilterQueryModel>
    ): FilterResult? {
        val preprocess = preprocess(context, filters)

        return processMute(post, preprocess)
    }

    private suspend fun processMute(
        post: Post,
        preprocess: List<PreProcessedFilter>
    ): FilterResult? {
        logger.trace("process mute post: {}", post)
        if (post.overview != null) {
            val processMute = processMute(post.overview, preprocess)

            if (processMute != null) {
                return processMute
            }
        }

        val processMute = processMute(post.text, preprocess)

        if (processMute != null) {
            return processMute
        }

        return null
    }

    override suspend fun processMutes(
        posts: List<Post>,
        context: List<FilterType>,
        filters: List<FilterQueryModel>
    ): Map<Post, FilterResult> {
        val preprocess = preprocess(context, filters)

        return posts.mapNotNull { it to (processMute(it, preprocess) ?: return@mapNotNull null) }.toMap()
    }

    private suspend fun processMute(string: String, filters: List<PreProcessedFilter>): FilterResult? {
        for (filter in filters) {
            val matchEntire = filter.regex.find(string)

            if (matchEntire != null) {
                return FilterResult(filter.filter, matchEntire.value)
            }
        }

        return null
    }

    private fun preprocess(context: List<FilterType>, filters: List<FilterQueryModel>): List<PreProcessedFilter> {
        val filterQueryModelList = filters
            .filter { it.context.any(context::contains) }
            .map {
                PreProcessedFilter(
                    it,
                    precompileRegex(it)
                )
            }

        return filterQueryModelList
    }

    private fun precompileRegex(filter: FilterQueryModel): Regex {
        logger.trace("precompile regex. filter: {}", filter)

        val regexList = mutableListOf<Regex>()

        val noneRegexStrings = mutableListOf<String>()
        val wholeRegexStrings = mutableListOf<String>()

        for (keyword in filter.keywords) {
            when (keyword.mode) {
                WHOLE_WORD -> wholeRegexStrings.add(keyword.keyword)
                REGEX -> regexList.add(Regex(keyword.keyword))
                NONE -> noneRegexStrings.add(keyword.keyword)
            }
        }

        val noneRegex = noneRegexStrings.joinToString("|", "(", ")")
        val wholeRegex = wholeRegexStrings.joinToString("|", "\\b(", ")\\b")

        val regex = if (noneRegexStrings.isNotEmpty() && wholeRegexStrings.isNotEmpty()) {
            Regex("$noneRegex|$wholeRegex")
        } else if (noneRegexStrings.isNotEmpty()) {
            noneRegex.toRegex()
        } else if (wholeRegexStrings.isNotEmpty()) {
            wholeRegex.toRegex()
        } else {
            null
        }

        if (regex != null) {
            regexList.add(regex)
        }

        val pattern = regexList.joinToString(")|(", "(", ")")
        logger.trace("precompiled regex {}", pattern)

        return Regex(pattern)
    }

    data class PreProcessedFilter(val filter: FilterQueryModel, val regex: Regex)

    companion object {
        private val logger = LoggerFactory.getLogger(MuteProcessServiceImpl::class.java)
    }
}
