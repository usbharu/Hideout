package dev.usbharu.hideout.core.infrastructure.exposedquery

import dev.usbharu.hideout.core.application.model.Reactions
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.infrastructure.exposedrepository.AbstractRepository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.CustomEmojis
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toCustomEmojiOrNull
import dev.usbharu.hideout.core.infrastructure.exposedrepository.toReaction
import dev.usbharu.hideout.core.query.reactions.ReactionsQueryService
import org.jetbrains.exposed.sql.selectAll
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Reactions as ExposedrepositoryReactions

@Repository
class ExposedReactionsQueryService : ReactionsQueryService, AbstractRepository() {
    override suspend fun findAllByPostId(postId: PostId): List<Reactions> {
        return query {
            ExposedrepositoryReactions.leftJoin(CustomEmojis).selectAll()
                .where { ExposedrepositoryReactions.postId eq postId.id }
                .groupBy {
                    it[ExposedrepositoryReactions.customEmojiId]?.toString()
                        ?: it[ExposedrepositoryReactions.unicodeEmoji]
                }
                .map { it.value }
                .map {
                    Reactions.of(
                        it.map { resultRow -> resultRow.toReaction() },
                        it.first().toCustomEmojiOrNull()
                    )
                }
        }
    }

    override val logger: Logger
        get() = Companion.logger

    companion object {
        private val logger = LoggerFactory.getLogger(ExposedReactionsQueryService::class.java)
    }
}