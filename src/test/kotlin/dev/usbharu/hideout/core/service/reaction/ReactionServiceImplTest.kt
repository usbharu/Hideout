package dev.usbharu.hideout.core.service.reaction


import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.emoji.UnicodeEmoji
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.PostBuilder

@ExtendWith(MockitoExtension::class)
class ReactionServiceImplTest {

    @Mock
    private lateinit var reactionRepository: ReactionRepository

    @Mock
    private lateinit var apReactionService: APReactionService

    @InjectMocks
    private lateinit var reactionServiceImpl: ReactionServiceImpl

    @Test
    fun `receiveReaction リアクションが存在しないとき保存する`() = runTest {

        val post = PostBuilder.of()

        whenever(reactionRepository.existByPostIdAndActor(eq(post.id), eq(post.actorId))).doReturn(
            false
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.receiveReaction(UnicodeEmoji("❤"), post.actorId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId)))
    }


    @Test
    fun `receiveReaction リアクションが既に作成されている場合削除して新しく作成`() = runTest() {
        val post = PostBuilder.of()
        whenever(reactionRepository.existByPostIdAndActor(eq(post.id), eq(post.actorId))).doReturn(
            true
        )

        val generateId = TwitterSnowflakeIdGenerateService.generateId()

        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.receiveReaction(UnicodeEmoji("❤"), post.actorId, post.id)

        verify(reactionRepository, times(1)).deleteByPostIdAndActorId(post.id, post.actorId)
        verify(reactionRepository, times(1)).save(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId))
    }

    @Test
    fun `sendReaction リアクションが存在しないとき保存して配送する`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            null
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction(UnicodeEmoji("❤"), post.actorId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId)))
    }

    @Test
    fun `sendReaction リアクションが存在するときは削除して保存して配送する`() = runTest {
        val post = PostBuilder.of()
        val id = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            Reaction(id, UnicodeEmoji("❤"), post.id, post.actorId)
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction(UnicodeEmoji("❤"), post.actorId, post.id)


        verify(reactionRepository, times(1)).delete(eq(Reaction(id, UnicodeEmoji("❤"), post.id, post.actorId)))
        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(id, UnicodeEmoji("❤"), post.id, post.actorId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, UnicodeEmoji("❤"), post.id, post.actorId)))
    }

    @Test
    fun `removeReaction リアクションが存在する場合削除して配送`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            Reaction(0, UnicodeEmoji("❤"), post.id, post.actorId)
        )

        reactionServiceImpl.removeReaction(post.actorId, post.id)

        verify(reactionRepository, times(1)).delete(eq(Reaction(0, UnicodeEmoji("❤"), post.id, post.actorId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(0, UnicodeEmoji("❤"), post.id, post.actorId)))
    }
}
