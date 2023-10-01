package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.hideout.entity.Reaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Primary
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
@Primary
class JdbcReactionRepositoryWrapper(private val reactionRepository: JdbcReactionRepository) : ReactionRepository {
    override suspend fun save(reaction: Reaction): Reaction {
        return withContext(Dispatchers.IO) {
            reactionRepository.save(reaction)
        }
    }

    override suspend fun delete(reaction: Reaction): Reaction {
        withContext(Dispatchers.IO) {
            reactionRepository.deleteById(reaction.id)
        }
        return reaction
    }

}

interface JdbcReactionRepository : CrudRepository<Reaction, Long>
