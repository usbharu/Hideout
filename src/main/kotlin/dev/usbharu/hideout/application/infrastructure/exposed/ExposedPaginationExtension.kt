package dev.usbharu.hideout.application.infrastructure.exposed

import org.jetbrains.exposed.sql.*

fun <S> Query.withPagination(page: Page, exp: ExpressionWithColumnType<S>): PaginationList<ResultRow, S> {
    page.limit?.let { limit(it) }
    val resultRows = if (page.minId != null) {
        page.maxId?.let { andWhere { exp.less(it) } }
        andWhere { exp.greater(page.minId!!) }
        reversed()
    } else {
        page.maxId?.let { andWhere { exp.less(it) } }
        page.sinceId?.let { andWhere { exp.greater(it) } }
        orderBy(exp, SortOrder.DESC)
        toList()
    }

    return PaginationList(resultRows, resultRows.firstOrNull()?.getOrNull(exp), resultRows.lastOrNull()?.getOrNull(exp))
}