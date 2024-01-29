package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.application.infrastructure.exposed.Page
import dev.usbharu.hideout.application.infrastructure.exposed.PaginationList
import dev.usbharu.hideout.core.domain.model.actor.ActorRepository
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
import dev.usbharu.hideout.core.service.media.MediaService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import dev.usbharu.hideout.core.service.user.UserService
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.domain.mastodon.model.generated.Relationship
import dev.usbharu.hideout.domain.mastodon.model.generated.Status
import dev.usbharu.hideout.mastodon.query.StatusQueryService
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class AccountApiServiceImplTest {

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var userService: UserService

    @Mock
    private lateinit var actorRepository: ActorRepository

    @Mock
    private lateinit var followerQueryService: FollowerQueryService

    @Mock
    private lateinit var statusQueryService: StatusQueryService

    @Spy
    private val transaction: Transaction = TestTransaction

    @Mock
    private lateinit var relationshipService: RelationshipService

    @Mock
    private lateinit var relationshipRepository: RelationshipRepository

    @Mock
    private lateinit var mediaService: MediaService

    @InjectMocks
    private lateinit var accountApiServiceImpl: AccountApiServiceImpl

    private val statusList = PaginationList<Status, Long>(
        listOf(
            Status(
                id = "",
                uri = "",
                createdAt = "",
                account = Account(
                    id = "",
                    username = "",
                    acct = "",
                    url = "",
                    displayName = "",
                    note = "",
                    avatar = "",
                    avatarStatic = "",
                    header = "",
                    headerStatic = "",
                    locked = false,
                    fields = emptyList(),
                    emojis = emptyList(),
                    bot = false,
                    group = false,
                    discoverable = true,
                    createdAt = "",
                    lastStatusAt = "",
                    statusesCount = 0,
                    followersCount = 0,
                    noindex = false,
                    moved = false,
                    suspendex = false,
                    limited = false,
                    followingCount = 0
                ),
                content = "",
                visibility = Status.Visibility.public,
                sensitive = false,
                spoilerText = "",
                mediaAttachments = emptyList(),
                mentions = emptyList(),
                tags = emptyList(),
                emojis = emptyList(),
                reblogsCount = 0,
                favouritesCount = 0,
                repliesCount = 0,
                url = "https://example.com",
                inReplyToId = null,
                inReplyToAccountId = null,
                language = "ja_JP",
                text = "Test",
                editedAt = null
            )
        ), null, null
    )

    @Test
    fun `accountsStatuses 非ログイン時は非公開投稿を見れない`() = runTest {
        val userId = 1234L

        whenever(
            statusQueryService.accountsStatus(
                accountId = eq(userId),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(false),
                page = any()
            )
        ).doReturn(
            statusList
        )


        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = null,
            Page.of()
        )

        assertThat(accountsStatuses).hasSize(1)

        verify(followerQueryService, never()).alreadyFollow(any(), any())
    }

    @Test
    fun `accountsStatuses ログイン時フォロワーじゃない場合は非公開投稿を見れない`() = runTest {
        val userId = 1234L
        val loginUser = 1L
        whenever(
            statusQueryService.accountsStatus(
                accountId = eq(userId),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(false),
                page = any()
            )
        ).doReturn(statusList)

        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = loginUser,
            Page.of()
        )

        assertThat(accountsStatuses).hasSize(1)
    }

    @Test
    fun `accountsStatuses ログイン時フォロワーの場合は非公開投稿を見れる`() = runTest {
        val userId = 1234L
        val loginUser = 2L
        whenever(
            statusQueryService.accountsStatus(
                accountId = eq(userId),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(true),
                page = any()
            )
        ).doReturn(statusList)

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(loginUser), eq(userId))).doReturn(
            dev.usbharu.hideout.core.domain.model.relationship.Relationship(
                actorId = loginUser,
                targetActorId = userId,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )
        )


        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = loginUser,
            Page.of()
        )

        assertThat(accountsStatuses).hasSize(1)
    }

    @Test
    fun `follow 未フォローの場合フォローリクエストが発生する`() = runTest {
        val userId = 1234L
        val followeeId = 1L

        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(followeeId), eq(userId))).doReturn(
            dev.usbharu.hideout.core.domain.model.relationship.Relationship(
                actorId = followeeId,
                targetActorId = userId,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )
        )
        whenever(relationshipRepository.findByUserIdAndTargetUserId(eq(userId), eq(followeeId))).doReturn(
            dev.usbharu.hideout.core.domain.model.relationship.Relationship(
                actorId = userId,
                targetActorId = followeeId,
                following = true,
                blocking = false,
                muting = false,
                followRequest = false,
                ignoreFollowRequestToTarget = false
            )
        )


        val follow = accountApiServiceImpl.follow(userId, followeeId)

        val expected = Relationship(
            id = followeeId.toString(),
            following = true,
            showingReblogs = true,
            notifying = false,
            followedBy = true,
            blocking = false,
            blockedBy = false,
            muting = false,
            mutingNotifications = false,
            requested = false,
            domainBlocking = false,
            endorsed = false,
            note = ""
        )
        assertThat(follow).isEqualTo(expected)

        verify(relationshipService, times(1)).followRequest(eq(userId), eq(followeeId))
    }

    @Test
    fun `relationships idが長すぎたら省略する`() = runTest {

        val relationships = accountApiServiceImpl.relationships(
            userid = 1234L,
            id = (1..30L).toList(),
            withSuspended = false
        )

        assertThat(relationships).hasSize(20)
    }

    @Test
    fun `relationships id0の場合即時return`() = runTest {
        val relationships = accountApiServiceImpl.relationships(
            userid = 1234L,
            id = emptyList(),
            withSuspended = false
        )

        assertThat(relationships).hasSize(0)
        verify(followerQueryService, never()).alreadyFollow(any(), any())
    }

    @Test
    fun `relationships idに指定されたアカウントの関係を取得する`() = runTest {

        val relationships = accountApiServiceImpl.relationships(
            userid = 1234L,
            id = (1..15L).toList(),
            withSuspended = false
        )

        assertThat(relationships).hasSize(15)
    }
}
