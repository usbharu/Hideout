package dev.usbharu.hideout.core.domain.model.post

import dev.usbharu.hideout.core.domain.event.post.PostEvent
import dev.usbharu.hideout.core.domain.model.actor.ActorId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import utils.AssertDomainEvent.assertContainsEvent
import utils.AssertDomainEvent.assertEmpty
import kotlin.test.assertEquals
import kotlin.test.assertNull

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
            post.visibleActors = setOf(ActorId(100))
        }
    }

    @Test
    fun ゔvisibilityがDIRECT以外の時visibleActorsを変更できない() {
        val post = TestPostFactory.create(visibility = Visibility.FOLLOWERS)

        post.visibleActors = setOf(ActorId(100))
        assertEmpty(post)

        val post2 = TestPostFactory.create(visibility = Visibility.UNLISTED)

        post2.visibleActors = setOf(ActorId(100))
        assertEmpty(post2)

        val post3 = TestPostFactory.create(visibility = Visibility.PUBLIC)

        post3.visibleActors = setOf(ActorId(100))
        assertEmpty(post3)
    }

    @Test
    fun visibilityがDIRECTの時visibleActorsを変更できる() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)

        post.visibleActors = setOf(ActorId(100))
        assertEquals(setOf(ActorId(100)), post.visibleActors)
    }

    @Test
    fun visibleActorsから削除されることはない() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT, visibleActors = listOf(100))

        post.visibleActors = setOf(ActorId(200))
        assertEquals(setOf(ActorId(100), ActorId(200)), post.visibleActors)
    }

    @Test
    fun visibleActorsに追加された時updateイベントが発生する() {
        val post = TestPostFactory.create(visibility = Visibility.DIRECT)

        post.visibleActors = setOf(ActorId(100))

        assertContainsEvent(post, PostEvent.update.eventName)
    }

    @Test
    fun hideがtrueのときcontetnがemptyを返す() {
        val post = TestPostFactory.create(hide = true)

        assertEquals(PostContent.empty, post.content)
    }

    @Test
    fun deletedがtrueの時contentをセットできない() {
        val post = TestPostFactory.create(deleted = true)

        assertThrows<IllegalArgumentException> {
            post.content = PostContent("test", "test", emptyList())
        }
    }

    @Test
    fun contentの内容が変更されたらupdateイベントが発生する() {
        val post = TestPostFactory.create()

        post.content = PostContent("test", "test", emptyList())
        assertContainsEvent(post, PostEvent.update.eventName)
    }

    @Test
    fun hideがtrueの時nullを返す() {
        val post = TestPostFactory.create(hide = true, overview = "aaaa")

        assertNull(post.overview)
    }

    @Test
    fun hideがfalseの時overviewを返す() {
        val post = TestPostFactory.create(hide = false, overview = "aaaa")

        assertEquals(PostOverview("aaaa"), post.overview)
    }

    @Test
    fun deletedがtrueのときセットできない() {
        val post = TestPostFactory.create(deleted = true)

        assertThrows<IllegalArgumentException> {
            post.overview = PostOverview("aaaa")
        }
    }

    @Test
    fun deletedがfalseのときセットできる() {
        val post = TestPostFactory.create(deleted = false)

        val overview = PostOverview("aaaa")
        assertDoesNotThrow {
            post.overview = overview
        }
        assertEquals(overview, post.overview)

        assertContainsEvent(post, PostEvent.update.eventName)
    }

    @Test
    fun overviewの内容が更新されなかった時イベントが発生しない() {
        val post = TestPostFactory.create(overview = "aaaa")
        post.overview = PostOverview("aaaa")
        assertEmpty(post)
    }




}