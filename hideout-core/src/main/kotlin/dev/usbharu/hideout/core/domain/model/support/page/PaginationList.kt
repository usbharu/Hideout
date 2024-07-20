package dev.usbharu.hideout.core.domain.model.support.page

class PaginationList<T, ID>(list: List<T>, val next: ID?, val prev: ID?) : List<T> by list
