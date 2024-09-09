package dev.usbharu.hideout.core.application.model

import dev.usbharu.hideout.core.domain.model.emoji.CustomEmoji
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import java.net.URI

data class Reactions(
    val postId: Long,
    val count: Int,
    val name: String,
    val domain: String,
    val url: URI?,
    val actorIds: List<Long>,
) {
    companion object {
        fun of(reactionList: List<Reaction>, customEmoji: CustomEmoji?): Reactions {
            val first = reactionList.first()
            return Reactions(
                postId = first.id.value,
                count = reactionList.size,
                name = customEmoji?.name ?: first.unicodeEmoji.name,
                domain = customEmoji?.domain?.domain ?: first.unicodeEmoji.domain.domain,
                url = customEmoji?.url,
                actorIds = reactionList.map { it.actorId.id }
            )
        }
    }
}
