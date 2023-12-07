package dev.usbharu.hideout.mastodon.service.account

import dev.usbharu.hideout.application.external.Transaction
import dev.usbharu.hideout.core.domain.model.user.UserRepository
import dev.usbharu.hideout.core.query.FollowerQueryService
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
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var followerQueryService: FollowerQueryService

    @Mock
    private lateinit var statusQueryService: StatusQueryService

    @Spy
    private val transaction: Transaction = TestTransaction

    @InjectMocks
    private lateinit var accountApiServiceImpl: AccountApiServiceImpl

    private val statusList = listOf(
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
    )

    @Test
    fun `accountsStatuses 非ログイン時は非公開投稿を見れない`() = runTest {
        val userId = 1234L

        whenever(
            statusQueryService.accountsStatus(
                accountId = eq(userId),
                maxId = isNull(),
                sinceId = isNull(),
                minId = isNull(),
                limit = eq(20),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(false)
            )
        ).doReturn(
            statusList
        )


        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            maxId = null,
            sinceId = null,
            minId = null,
            limit = 20,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = null
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
                maxId = isNull(),
                sinceId = isNull(),
                minId = isNull(),
                limit = eq(20),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(false)
            )
        ).doReturn(statusList)

        whenever(followerQueryService.alreadyFollow(eq(userId), eq(loginUser))).doReturn(false)


        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            maxId = null,
            sinceId = null,
            minId = null,
            limit = 20,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = loginUser
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
                maxId = isNull(),
                sinceId = isNull(),
                minId = isNull(),
                limit = eq(20),
                onlyMedia = eq(false),
                excludeReplies = eq(false),
                excludeReblogs = eq(false),
                pinned = eq(false),
                tagged = isNull(),
                includeFollowers = eq(true)
            )
        ).doReturn(statusList)

        whenever(followerQueryService.alreadyFollow(eq(userId), eq(loginUser))).doReturn(true)


        val accountsStatuses = accountApiServiceImpl.accountsStatuses(
            userid = userId,
            maxId = null,
            sinceId = null,
            minId = null,
            limit = 20,
            onlyMedia = false,
            excludeReplies = false,
            excludeReblogs = false,
            pinned = false,
            tagged = null,
            loginUser = loginUser
        )

        assertThat(accountsStatuses).hasSize(1)
    }

    @Test
    fun `follow 既にフォローしている場合は何もしない`() = runTest {
        val userId = 1234L
        val followeeId = 1L

        whenever(followerQueryService.alreadyFollow(eq(followeeId), eq(userId))).doReturn(true)

        whenever(followerQueryService.alreadyFollow(eq(userId), eq(followeeId))).doReturn(true)

        whenever(userRepository.findFollowRequestsById(eq(followeeId), eq(userId))).doReturn(false)

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

        verify(userService, never()).followRequest(any(), any())
    }

    @Test
    fun `follow 未フォローの場合フォローリクエストが発生する`() = runTest {
        val userId = 1234L
        val followeeId = 1L

        whenever(followerQueryService.alreadyFollow(eq(followeeId), eq(userId))).doReturn(false)

        whenever(userService.followRequest(eq(followeeId), eq(userId))).doReturn(true)

        whenever(followerQueryService.alreadyFollow(eq(userId), eq(followeeId))).doReturn(true)

        whenever(userRepository.findFollowRequestsById(eq(followeeId), eq(userId))).doReturn(false)

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

        verify(userService, times(1)).followRequest(eq(followeeId), eq(userId))
    }

    @Test
    fun `relationships idが長すぎたら省略する`() = runTest {
        whenever(followerQueryService.alreadyFollow(any(), any())).doReturn(true)

        whenever(userRepository.findFollowRequestsById(any(), any())).doReturn(true)

        val relationships = accountApiServiceImpl.relationships(
            userid = 1234L,
            id = (1..30L).toList(),
            withSuspended = false
        )

        assertThat(relationships).hasSizeLessThanOrEqualTo(20)
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
        verify(userRepository, never()).findFollowRequestsById(any(), any())
    }

    @Test
    fun `relationships idに指定されたアカウントの関係を取得する`() = runTest {
        whenever(followerQueryService.alreadyFollow(any(), any())).doReturn(true)

        whenever(userRepository.findFollowRequestsById(any(), any())).doReturn(true)

        val relationships = accountApiServiceImpl.relationships(
            userid = 1234L,
            id = (1..15L).toList(),
            withSuspended = false
        )

        assertThat(relationships).hasSize(15)
    }
}
