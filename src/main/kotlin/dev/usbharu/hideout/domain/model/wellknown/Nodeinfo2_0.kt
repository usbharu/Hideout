@file:Suppress("ClassName")

package dev.usbharu.hideout.domain.model.wellknown

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
