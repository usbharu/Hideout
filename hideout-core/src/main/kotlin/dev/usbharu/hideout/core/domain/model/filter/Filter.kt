package dev.usbharu.hideout.core.domain.model.filter

import dev.usbharu.hideout.core.domain.model.filter.Filter.Companion.Action.SET_KEYWORDS
import dev.usbharu.hideout.core.domain.model.filter.FilterMode.*
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetail
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId

class Filter(
    val id: FilterId,
    val userDetailId: UserDetailId,
    var name: FilterName,
    val filterContext: List<FilterContext>,
    val filterAction: FilterAction,
    filterKeywords: Set<FilterKeyword>
) {
    var filterKeywords = filterKeywords
        private set

    fun setFilterKeywords(filterKeywords: Set<FilterKeyword>, user: UserDetail) {
        require(isAllow(user, SET_KEYWORDS, this))
        this.filterKeywords = filterKeywords
    }

    fun compileFilter(): Regex {
        val words = mutableListOf<String>()
        val wholeWords = mutableListOf<String>()
        val regexes = mutableListOf<String>()

        for (filterKeyword in filterKeywords) {
            when (filterKeyword.mode) {
                WHOLE_WORD -> wholeWords.add(filterKeyword.keyword.keyword)
                REGEX -> regexes.add(filterKeyword.keyword.keyword)
                NONE -> words.add(filterKeyword.keyword.keyword)
            }
        }

        return (wholeWords + regexes + wholeWords)
            .joinToString("|")
            .toRegex()
    }

    companion object {
        fun isAllow(user: UserDetail, action: Action, resource: Filter): Boolean {
            return when (action) {
                SET_KEYWORDS -> resource.userDetailId == user.id
            }
        }

        enum class Action {
            SET_KEYWORDS
        }
    }
}
