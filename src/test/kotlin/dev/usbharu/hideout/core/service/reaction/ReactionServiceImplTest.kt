package dev.usbharu.hideout.core.service.reaction


import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.exceptions.ExposedSQLException
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

        whenever(reactionRepository.existByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            false
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.receiveReaction("❤", "example.com", post.actorId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.actorId)))
    }

    @Test
    fun `receiveReaction リアクションが既に作成されていることを検知出来ずに例外が発生した場合は何もしない`() = runTest {
        val post = PostBuilder.of()

        whenever(reactionRepository.existByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            false
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(
            reactionRepository.save(
                eq(
                    Reaction(
                        id = generateId,
                        emojiId = 0,
                        postId = post.id,
                        actorId = post.actorId
                    )
                )
            )
        ).doAnswer {
            throw ExposedSQLException(
                null,
                emptyList(), mock()
            )
        }
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.receiveReaction("❤", "example.com", post.actorId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.actorId)))
    }

    @Test
    fun `receiveReaction リアクションが既に作成されている場合は何もしない`() = runTest() {
        val post = PostBuilder.of()
        whenever(reactionRepository.existByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            true
        )

        reactionServiceImpl.receiveReaction("❤", "example.com", post.actorId, post.id)

        verify(reactionRepository, never()).save(any())
    }

    @Test
    fun `sendReaction リアクションが存在しないとき保存して配送する`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            null
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction("❤", post.actorId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.actorId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, 0, post.id, post.actorId)))
    }

    @Test
    fun `sendReaction リアクションが存在するときは削除して保存して配送する`() = runTest {
        val post = PostBuilder.of()
        val id = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            Reaction(id, 0, post.id, post.actorId)
        )
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction("❤", post.actorId, post.id)


        verify(reactionRepository, times(1)).delete(eq(Reaction(id, 0, post.id, post.actorId)))
        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.actorId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(id, 0, post.id, post.actorId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, 0, post.id, post.actorId)))
    }

    @Test
    fun `removeReaction リアクションが存在する場合削除して配送`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionRepository.findByPostIdAndActorIdAndEmojiId(eq(post.id), eq(post.actorId), eq(0))).doReturn(
            Reaction(0, 0, post.id, post.actorId)
        )

        reactionServiceImpl.removeReaction(post.actorId, post.id)

        verify(reactionRepository, times(1)).delete(eq(Reaction(0, 0, post.id, post.actorId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(0, 0, post.id, post.actorId)))
    }
}
