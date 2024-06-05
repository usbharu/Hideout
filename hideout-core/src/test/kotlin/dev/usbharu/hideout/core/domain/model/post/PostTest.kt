package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utils.AssertDomainEvent.assertContainsEvent
import utils.AssertDomainEvent.assertEmpty
import kotlin.test.assertEquals

class PostTest {
    @Test
    fun deletedがtrueのときghostのidが返される() {
        val post = TestPostFactory.create(deleted = true)

        assertEquals(ActorId.ghost, post.actorId)
    }

    @Test
    fun deletedがfalseの時actorのIDが返される() {
        val post = TestPostFactory.create(deleted = false, actorId = 100)

        assertEquals(ActorId(100), post.actorId)
    }

    @Test
    fun visibilityがDIRECTのとき変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.PUBLIC
        }
        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.UNLISTED
        }
        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.FOLLOWERS
        }
    }

    @Test
    fun visibilityを小さくすることはできないPUBLIC() {
        val post = TestPostFactory.create(visibility = Visibility.PUBLIC)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.DIRECT
        }
        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.UNLISTED
        }
        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.FOLLOWERS
        }
    }

    @Test
    fun visibilityを小さくすることはできないUNLISTED() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.DIRECT
        }
        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.FOLLOWERS
        }
    }

    @Test
    fun visibilityを小さくすることはできないFOLLOWERS() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.DIRECT
        }
    }

    @Test
    fun visibilityをDIRECTにあとからすることはできない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.DIRECT
        }
    }

    @Test
    fun visibilityを大きくすることができるFOLLOWERS() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)

        assertDoesNotThrow {
            post.visibility = Visibility.UNLISTED
        }

        val post2 = TestPostFactory.create(visibility = Visibility.FOLLOWERS)

        assertDoesNotThrow {
            post2.visibility = Visibility.PUBLIC
        }
    }

    @Test
    fun visibilityを大きくすることができるUNLISTED() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)

        assertDoesNotThrow {
            post.visibility = Visibility.PUBLIC
        }
    }

    @Test
    fun deletedがtrueのときvisibilityを変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED, deleted = true)

        assertThrows<IllegalArgumentException> {
            post.visibility = Visibility.PUBLIC
        }
    }

    @Test
    fun visibilityが変更されない限りドメインイベントは発生しない() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)

        post.visibility = Visibility.UNLISTED
        assertEmpty(post)

    }

    @Test
    fun visibilityが変更されるとupdateイベントが発生する() {
        val post = TestPostFactory.create(visibility = Visibility.UNLISTED)
        post.visibility = Visibility.PUBLIC

        assertContainsEvent(post, PostEvent.update.eventName)
    }

    @Test
    fun deletedがtrueのときvisibleActorsを変更できない() {
        val post = TestPostFactory.create(deleted = true)

        assertThrows<IllegalArgumentException> {
            post.visibleActors = listOf(ActorId(100))
        }
    }
}