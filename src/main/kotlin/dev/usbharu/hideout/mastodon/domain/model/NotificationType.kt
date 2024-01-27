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
}
