package dev.usbharu.hideout.core.domain.service.post

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.acct.Acct
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.FromApi
import dev.usbharu.hideout.core.domain.model.userdetails.UserDetailId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class DefaultPostReadAccessControlTest {
    @InjectMocks
    lateinit var service: DefaultPostReadAccessControl

    @Mock
    lateinit var relationshipRepository: RelationshipRepository

    @Test
    fun ブロックされてたら見れない() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship(
                actorId = ActorId(1),
                targetActorId = ActorId(2),
                following = false,
                blocking = true,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false,
            )
        )

        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun PublicかUnlistedなら見れる() = runTest {
        val actual = service.isAllow(TestPostFactory.create(visibility = Visibility.PUBLIC), Anonymous)
        assertTrue(actual)

        val actual2 = service.isAllow(TestPostFactory.create(visibility = Visibility.UNLISTED), Anonymous)
        assertTrue(actual2)
    }

    @Test
    fun FollowersかDirecのときAnonymousなら見れない() = runTest {
        val actual = service.isAllow(TestPostFactory.create(visibility = Visibility.FOLLOWERS), Anonymous)
        assertFalse(actual)

        val actual2 = service.isAllow(TestPostFactory.create(visibility = Visibility.DIRECT), Anonymous)
        assertFalse(actual2)
    }

    @Test
    fun DirectでvisibleActorsに含まれていたら見れる() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT, visibleActors = listOf(2)),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertTrue(actual)
    }

    @Test
    fun DirectでvisibleActorsに含まれていなかったら見れない() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.DIRECT, visibleActors = listOf(3)),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun Followersでフォロワーなら見れる() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship.default(
                actorId = ActorId(1),
                targetActorId = ActorId(2)
            )
        )
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(2), ActorId(1))).doReturn(
            Relationship(
                actorId = ActorId(2),
                targetActorId = ActorId(1),
                following = true,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false
            )
        )


        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertTrue(actual)
    }

    @Test
    fun relationshipが見つからない場合見れない() = runTest {
        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }

    @Test
    fun フォロワーじゃない場合は見れない() = runTest {
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(1), ActorId(2))).doReturn(
            Relationship.default(
                actorId = ActorId(1),
                targetActorId = ActorId(2)
            )
        )
        whenever(relationshipRepository.findByActorIdAndTargetId(ActorId(2), ActorId(1))).doReturn(
            Relationship(
                actorId = ActorId(2),
                targetActorId = ActorId(1),
                following = false,
                blocking = false,
                muting = false,
                followRequesting = false,
                mutingFollowRequest = false
            )
        )


        val actual = service.isAllow(
            TestPostFactory.create(actorId = 1, visibility = Visibility.FOLLOWERS),
            FromApi(ActorId(2), UserDetailId(2), Acct("test", "example.com"))
        )

        assertFalse(actual)
    }
}