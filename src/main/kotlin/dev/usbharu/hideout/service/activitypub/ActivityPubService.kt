package dev.usbharu.hideout.service.activitypub

import dev.usbharu.hideout.domain.model.ActivityPubResponse

interface ActivityPubService {
    fun parseActivity(json:String): ActivityType

    fun processActivity(json:String, type: ActivityType): ActivityPubResponse?
}

enum class ActivityType {
    Accept,
    Add,
    Announce,
    Arrive,
    Block,
    Create,
    Delete,
    Dislike,
    Flag,
    Follow,
    Ignore,
    Invite,
    Join,
    Leave,
    Like,
    Listen,
    Move,
    Offer,
    Question,
    Reject,
    Read,
    Remove,
    TentativeReject,
    TentativeAccept,
    Travel,
    Undo,
    Update,
    View,
    Other
}
