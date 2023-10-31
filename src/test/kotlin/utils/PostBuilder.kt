package utils

import dev.usbharu.hideout.config.CharacterLimit
import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.domain.model.hideout.entity.Visibility
import dev.usbharu.hideout.service.core.TwitterSnowflakeIdGenerateService
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
            userId = userId,
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
