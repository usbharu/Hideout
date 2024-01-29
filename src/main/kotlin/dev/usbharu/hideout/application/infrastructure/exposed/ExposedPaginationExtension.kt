package dev.usbharu.hideout.application.infrastructure.exposed

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere

fun Query.pagination(page: Page, exp: ExpressionWithColumnType<Long>): Query {
    if (page.minId != null) {
        page.maxId?.let { andWhere { exp.less(it) } }
        page.minId?.let { andWhere { exp.greater(it) } }
    } else {
        page.maxId?.let { andWhere { exp.less(it) } }
        page.sinceId?.let { andWhere { exp.greater(it) } }
        this.orderBy(exp, SortOrder.DESC)
    }
    page.limit?.let { limit(it) }
    return this
}

fun Query.pagination(page: Page, exp: ExpressionWithColumnType<EntityID<Long>>): Query {
    if (page.minId != null) {
        page.maxId?.let { andWhere { exp.less(it) } }
        page.minId?.let { andWhere { exp.greater(it) } }
    } else {
        page.maxId?.let { andWhere { exp.less(it) } }
        page.sinceId?.let { andWhere { exp.greater(it) } }
        this.orderBy(exp, SortOrder.DESC)
    }
    page.limit?.let { limit(it) }
    return this
}
