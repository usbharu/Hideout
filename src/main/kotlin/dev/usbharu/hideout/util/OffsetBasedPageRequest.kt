package dev.usbharu.hideout.util

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort


class OffsetBasedPageRequest(limit: Int, offset: Int) : Pageable {
    private val pageSize: Int
    private val offset: Int

    // Constructor could be expanded if sorting is needed
    private val sort: Sort = Sort.by(Sort.Direction.DESC, "id")

    init {
        require(limit >= 1) { "Limit must not be less than one!" }
        require(offset >= 0) { "Offset index must not be less than zero!" }
        pageSize = limit
        this.offset = offset
    }


    override fun getPageNumber(): Int = offset / pageSize

    override fun getPageSize(): Int = pageSize

    override fun getOffset(): Long = offset.toLong()

    override fun getSort(): Sort = sort

    override operator fun next(): Pageable {
        return OffsetBasedPageRequest(pageSize, (getOffset() + pageSize).toInt())
    }

    fun previous(): Pageable {
        return if (hasPrevious()) OffsetBasedPageRequest(pageSize, (getOffset() - pageSize).toInt()) else this
    }

    override fun previousOrFirst(): Pageable {
        return if (hasPrevious()) previous() else first()
    }

    override fun first(): Pageable {
        return OffsetBasedPageRequest(pageSize, 0)
    }

    override fun withPage(pageNumber: Int): Pageable {
        TODO("Not yet implemented")
    }

    override fun hasPrevious(): Boolean {
        return offset > pageSize
    }
}
