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

import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.net.MalformedURLException
import java.net.URL

object AssertionUtil {

    @JvmStatic
    fun assertUserExist(username: String, domain: String): Boolean {
        val s = try {
            val url = URL(domain)
            url.host + ":" + url.port.toString().takeIf { it != "-1" }.orEmpty()
        } catch (e: MalformedURLException) {
            domain
        }

        val selectAll = Actors.selectAll()
        println(selectAll.fetchSize)

        println(selectAll.toList().size)

        selectAll.map { "@${it[Actors.name]}@${it[Actors.domain]}" }.forEach { println(it) }

        return Actors.selectAll().where { Actors.name eq username and (Actors.domain eq s) }.empty().not()
    }
}
