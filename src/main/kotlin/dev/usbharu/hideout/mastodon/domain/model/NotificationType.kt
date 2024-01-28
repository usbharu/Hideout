package dev.usbharu.hideout.mastodon.domain.model

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
