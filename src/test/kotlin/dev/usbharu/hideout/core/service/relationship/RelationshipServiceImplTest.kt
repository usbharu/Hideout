package dev.usbharu.hideout.core.service.relationship

import dev.usbharu.hideout.activitypub.service.activity.accept.ApSendAcceptService
import dev.usbharu.hideout.activitypub.service.activity.block.APSendBlockService
import dev.usbharu.hideout.activitypub.service.activity.follow.APSendFollowService
import dev.usbharu.hideout.activitypub.service.activity.reject.ApSendRejectService
import dev.usbharu.hideout.activitypub.service.activity.undo.APSendUndoService
import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.follow.SendFollowDto
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.UserBuilder
import java.net.URL

@ExtendWith(MockitoExtension::class)
class RelationshipServiceImplTest {

    @Spy
    private val applicationConfig = ApplicationConfig(URL("https://example.com"))

    @Mock
    private lateinit var actorQueryService: ActorQueryService

    @Mock
    private lateinit var relationshipRepository: RelationshipRepository

    @Mock
    private lateinit var apSendFollowService: APSendFollowService

    @Mock
    private lateinit var apSendBlockService: APSendBlockService

    @Mock
    private lateinit var apSendAcceptService: ApSendAcceptService

    @Mock
    private lateinit var apSendRejectService: ApSendRejectService

    @Mock
    private lateinit var apSendUndoService: APSendUndoService

    @InjectMocks
    private lateinit var relationshipServiceImpl: RelationshipServiceImpl

    @Test
    fun `followRequest ローカルの場合followRequestフラグがtrueで永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = true,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )
    }

    @Test
    fun `followRequest リモートの場合Followアクティビティが配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)
        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = true,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendFollowService, times(1)).sendFollow(eq(SendFollowDto(localUser, remoteUser)))
    }

    @Test
    fun `followRequest ブロックされている場合フォローリクエスト出来ない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(null)
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = true,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `followRequest ブロックしている場合フォローリクエスト出来ない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = true,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `followRequest 既にフォローしている場合は念の為フォロー承認を自動で行う`() = runTest {
        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(remoteUser)
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(localUser)
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = true,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendAcceptService, times(1)).sendAcceptFollow(eq(localUser), eq(remoteUser))
        verify(apSendFollowService, never()).sendFollow(any())
    }

    @Test
    fun `followRequest フォローリクエスト無視の場合は無視する`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = true
            )
        )

        relationshipServiceImpl.followRequest(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `block ローカルユーザーの場合永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))

        relationshipServiceImpl.block(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = true,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )
    }

    @Test
    fun `block リモートユーザーの場合永続化されて配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)
        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        relationshipServiceImpl.block(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = true,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendBlockService, times(1)).sendBlock(eq(localUser), eq(remoteUser))
    }

    @Test
    fun `acceptFollowRequest ローカルユーザーの場合永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = true,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)

        verify(relationshipRepository, times(1)).save(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        verify(apSendAcceptService, never()).sendAcceptFollow(any(), any())
    }

    @Test
    fun `acceptFollowRequest リモートユーザーの場合永続化されて配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)
        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = true,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 5678,
                    targetActorId = 1234,
                    following = true,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendAcceptService, times(1)).sendAcceptFollow(eq(localUser), eq(remoteUser))
    }

    @Test
    fun `acceptFollowRequest Relationshipが存在しないときは何もしない`() = runTest {
        relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)

        verify(apSendAcceptService, never()).sendAcceptFollow(any(), any())
    }

    @Test
    fun `acceptFollowRequest フォローリクエストが存在せずforceがfalseのとき何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                5678, 1234, false, false, false, false, false
            )
        )

        relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)

        verify(apSendAcceptService, never()).sendAcceptFollow(any(), any())
    }

    @Test
    fun `acceptFollowRequest フォローリクエストが存在せずforceがtrueのときフォローを承認する`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.remoteUserOf(domain = "remote.example.com"))

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                5678, 1234, false, false, false, false, false
            )
        )

        relationshipServiceImpl.acceptFollowRequest(1234, 5678, true)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 5678,
                    targetActorId = 1234,
                    following = true,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )
    }

    @Test
    fun `acceptFollowRequest ブロックしている場合は何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                5678, 1234, false, true, false, true, false
            )
        )

        assertThrows<IllegalStateException> {
            relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)
        }

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `acceptFollowRequest ブロックされている場合は何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                1234, 5678, false, false, false, true, false
            )
        )

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                5678, 1234, false, true, false, true, false
            )
        )

        assertThrows<IllegalStateException> {
            relationshipServiceImpl.acceptFollowRequest(1234, 5678, false)
        }

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `rejectFollowRequest ローカルユーザーの場合永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = true,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.rejectFollowRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 5678,
                    targetActorId = 1234,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendRejectService, never()).sendRejectFollow(any(), any())
    }

    @Test
    fun `rejectFollowRequest リモートユーザーの場合永続化されて配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)

        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = true,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.rejectFollowRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 5678,
                    targetActorId = 1234,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendRejectService, times(1)).sendRejectFollow(eq(localUser), eq(remoteUser))
    }

    @Test
    fun `rejectFollowRequest Relationshipが存在しないとき何もしない`() = runTest {

        relationshipServiceImpl.rejectFollowRequest(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `rejectFollowRequest フォローリクエストが存在しない場合何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(5678), eq(1234))).doReturn(
            Relationship(
                actorId = 5678,
                targetActorId = 1234,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.rejectFollowRequest(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `ignoreFollowRequest 永続化される`() = runTest {
        relationshipServiceImpl.ignoreFollowRequest(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = true
                )
            )
        )
    }

    @Test
    fun `unfollow ローカルユーザーの場合永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unfollow(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendUndoService, never()).sendUndoFollow(any(), any())
    }

    @Test
    fun `unfollow リモートユーザー場合永続化されて配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)

        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unfollow(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendUndoService, times(1)).sendUndoFollow(eq(localUser), eq(remoteUser))
    }

    @Test
    fun `unfollow Relationshipが存在しないときは何もしない`() = runTest {
        relationshipServiceImpl.unfollow(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `unfollow フォローしていなかった場合は何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unfollow(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `unblock ローカルユーザーの場合永続化される`() = runTest {
        whenever(actorQueryService.findById(eq(5678))).doReturn(UserBuilder.localUserOf(domain = "example.com"))
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = true,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unblock(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )

        verify(apSendUndoService, never()).sendUndoBlock(any(), any())
    }

    @Test
    fun `unblock リモートユーザーの場合永続化されて配送される`() = runTest {
        val localUser = UserBuilder.localUserOf(domain = "example.com")
        whenever(actorQueryService.findById(eq(1234))).doReturn(localUser)

        val remoteUser = UserBuilder.remoteUserOf(domain = "remote.example.com")
        whenever(actorQueryService.findById(eq(5678))).doReturn(remoteUser)

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = true,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unblock(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    1234,
                    5678,
                    false,
                    false,
                    false,
                    false,
                    false
                )
            )
        )

        verify(apSendUndoService, times(1)).sendUndoBlock(eq(localUser), eq(remoteUser))
    }

    @Test
    fun `unblock Relationshipがない場合何もしない`() = runTest {
        relationshipServiceImpl.unblock(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `unblock ブロックしていない場合は何もしない`() = runTest {
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unblock(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }

    @Test
    fun `mute ミュートが永続化される`() = runTest {
        relationshipServiceImpl.mute(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = true,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )
    }

    @Test
    fun `unmute 永続化される`() = runTest {

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(1234), eq(5678))).doReturn(
            Relationship(
                actorId = 1234,
                targetActorId = 5678,
                following = false,
                blocking = false,
                muting = true,
                followRequest = false,
                ignoreFollowRequestFromTarget = false
            )
        )

        relationshipServiceImpl.unmute(1234, 5678)

        verify(relationshipRepository, times(1)).save(
            eq(
                Relationship(
                    actorId = 1234,
                    targetActorId = 5678,
                    following = false,
                    blocking = false,
                    muting = false,
                    followRequest = false,
                    ignoreFollowRequestFromTarget = false
                )
            )
        )
    }

    @Test
    fun `unmute Relationshipが存在しない場合は何もしない`() = runTest {
        relationshipServiceImpl.unmute(1234, 5678)

        verify(relationshipRepository, never()).save(any())
    }
}
