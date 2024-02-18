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

package dev.usbharu.hideout.mastodon.domain.model

@Suppress("EnumEntryName", "EnumNaming", "EnumEntryNameCase")
enum class NotificationType {
    mention,
    status,
    reblog,
    follow,
    follow_request,
    favourite,
    poll,
    update,
    admin_sign_up,
    admin_report,
    severed_relationships;

    companion object {
        fun parse(string: String): NotificationType? = when (string) {
            "mention" -> mention
            "status" -> status
            "reblog" -> reblog
            "follow" -> follow
            "follow_request" -> follow_request
            "favourite" -> favourite
            "poll" -> poll
            "update" -> update
            "admin.sign_up" -> admin_sign_up
            "admin.report" -> admin_report
            "servered_relationships" -> severed_relationships
            else -> null
        }
    }
}
