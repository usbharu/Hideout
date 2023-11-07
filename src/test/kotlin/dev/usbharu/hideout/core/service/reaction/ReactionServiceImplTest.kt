package dev.usbharu.hideout.core.service.reaction


import dev.usbharu.hideout.activitypub.service.activity.like.APReactionService
import dev.usbharu.hideout.application.service.id.TwitterSnowflakeIdGenerateService
import dev.usbharu.hideout.core.domain.model.reaction.Reaction
import dev.usbharu.hideout.core.domain.model.reaction.ReactionRepository
import dev.usbharu.hideout.core.query.ReactionQueryService
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

    @Mock
    private lateinit var reactionQueryService: ReactionQueryService

    @InjectMocks
    private lateinit var reactionServiceImpl: ReactionServiceImpl

    @Test
    fun `receiveReaction リアクションが存在しないとき保存する`() = runTest {

        val post = PostBuilder.of()

        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(false)
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.receiveReaction("❤", "example.com", post.userId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.userId)))
    }

    @Test
    fun `receiveReaction リアクションが既に作成されていることを検知出来ずに例外が発生した場合は何もしない`() = runTest {
        val post = PostBuilder.of()

        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(false)
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(
            reactionRepository.save(
                eq(
                    Reaction(
                        id = generateId,
                        emojiId = 0,
                        postId = post.id,
                        userId = post.userId
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

        reactionServiceImpl.receiveReaction("❤", "example.com", post.userId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.userId)))
    }

    @Test
    fun `receiveReaction リアクションが既に作成されている場合は何もしない`() = runTest() {
        val post = PostBuilder.of()
        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(true)

        reactionServiceImpl.receiveReaction("❤", "example.com", post.userId, post.id)

        verify(reactionRepository, never()).save(any())
    }

    @Test
    fun `sendReaction リアクションが存在しないとき保存して配送する`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(false)
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction("❤", post.userId, post.id)

        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.userId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, 0, post.id, post.userId)))
    }

    @Test
    fun `sendReaction リアクションが存在するときは削除して保存して配送する`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(true)
        val generateId = TwitterSnowflakeIdGenerateService.generateId()
        whenever(reactionRepository.generateId()).doReturn(generateId)

        reactionServiceImpl.sendReaction("❤", post.userId, post.id)


        verify(reactionRepository, times(1)).delete(eq(Reaction(generateId, 0, post.id, post.userId)))
        verify(reactionRepository, times(1)).save(eq(Reaction(generateId, 0, post.id, post.userId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(generateId, 0, post.id, post.userId)))
        verify(apReactionService, times(1)).reaction(eq(Reaction(generateId, 0, post.id, post.userId)))
    }

    @Test
    fun `removeReaction リアクションが存在する場合削除して配送`() = runTest {
        val post = PostBuilder.of()
        whenever(reactionQueryService.reactionAlreadyExist(eq(post.id), eq(post.userId), eq(0))).doReturn(true)

        reactionServiceImpl.removeReaction(post.userId, post.id)

        verify(reactionRepository, times(1)).delete(eq(Reaction(0, 0, post.id, post.userId)))
        verify(apReactionService, times(1)).removeReaction(eq(Reaction(0, 0, post.id, post.userId)))
    }
}
