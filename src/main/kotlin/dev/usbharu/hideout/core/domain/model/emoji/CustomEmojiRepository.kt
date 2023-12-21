package dev.usbharu.hideout.core.domain.model.emoji

interface CustomEmojiRepository {
    suspend fun save(customEmoji: CustomEmoji): CustomEmoji
    suspend fun findById(id: Long): CustomEmoji?
    suspend fun delete(customEmoji: CustomEmoji)
}
