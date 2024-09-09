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
                first.id.value,
                reactionList.size,
                customEmoji?.name ?: first.unicodeEmoji.name,
                customEmoji?.domain?.domain ?: first.unicodeEmoji.domain.domain,
                customEmoji?.url,
                reactionList.map { it.actorId.id }
            )
        }
    }
}
