package utils

import dev.usbharu.hideout.application.config.CharacterLimit
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import kotlinx.coroutines.runBlocking
import java.time.Instant

object PostBuilder {

    private val postBuilder = Post.PostBuilder(CharacterLimit())

    private val idGenerator = TwitterSnowflakeIdGenerateService

    fun of(
        id: Long = generateId(),
        userId: Long = generateId(),
        overview: String? = null,
        text: String = "Hello World",
        createdAt: Long = Instant.now().toEpochMilli(),
        visibility: Visibility = Visibility.PUBLIC,
        url: String = "https://example.com/users/$userId/posts/$id"
    ): Post {
        return postBuilder.of(
            id = id,
            actorId = userId,
            overview = overview,
            text = text,
            createdAt = createdAt,
            visibility = visibility,
            url = url,
        )
    }

    private fun generateId(): Long = runBlocking {
        idGenerator.generateId()
    }
}
