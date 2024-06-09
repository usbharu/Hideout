package dev.usbharu.hideout.core.domain.service.filter

import dev.usbharu.hideout.core.domain.model.filter.Filter
import dev.usbharu.hideout.core.domain.model.filter.FilterContext
import dev.usbharu.hideout.core.domain.model.filter.FilteredPost
import dev.usbharu.hideout.core.domain.model.post.Post

interface IFilterDomainService {
    fun apply(post: Post, context: FilterContext, filters: List<Filter>): FilteredPost
    fun applyAll(postList: List<Post>, context: FilterContext, filters: List<Filter>): List<FilteredPost>
}

class FilterDomainService : IFilterDomainService {
    override fun apply(post: Post, context: FilterContext, filters: List<Filter>): FilteredPost {
        filters.filter { it.filterContext.contains(context) }
    }

    override fun applyAll(postList: List<Post>, context: FilterContext, filters: List<Filter>): List<FilteredPost> {
        TODO("Not yet implemented")
    }

}