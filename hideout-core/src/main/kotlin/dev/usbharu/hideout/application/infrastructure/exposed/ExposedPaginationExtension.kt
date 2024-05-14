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