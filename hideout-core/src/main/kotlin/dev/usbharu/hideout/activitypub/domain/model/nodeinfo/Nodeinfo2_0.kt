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

@file:Suppress("ClassName")

package dev.usbharu.hideout.activitypub.domain.model.nodeinfo

@Suppress("ClassNaming")
data class Nodeinfo2_0(
    val version: String,
    val software: Software,
    val protocols: List<String>,
    val services: Services,
    val openRegistrations: Boolean,
    val usage: Usage,
    val metadata: Metadata
) {
    data class Software(
        val name: String,
        val version: String
    )

    data class Services(
        val inbound: List<String>,
        val outbound: List<String>
    )

    data class Usage(
        val users: Users,
        val localPosts: Int,
        val localComments: Int
    ) {
        data class Users(
            val total: Int,
            val activeHalfYear: Int,
            val activeMonth: Int
        )
    }

    data class Metadata(
        val nodeName: String,
        val nodeDescription: String,
        val maintainer: Maintainer,
        val langs: List<String>,
        val tosUrl: String,
        val repositoryUrl: String,
        val feedbackUrl: String,
    ) {
        data class Maintainer(
            val name: String,
            val email: String
        )
    }
}
