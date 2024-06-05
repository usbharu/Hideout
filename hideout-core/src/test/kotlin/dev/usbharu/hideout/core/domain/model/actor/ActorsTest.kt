package dev.usbharu.hideout.core.domain.model.actor

import dev.usbharu.hideout.core.domain.event.actor.ActorEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import utils.AssertDomainEvent.assertContainsEvent
import utils.AssertDomainEvent.assertEmpty
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class ActorsTest {
    @Test
    fun suspendがtrueのときactorSuspendイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        actor.suspend = true

        assertContainsEvent(actor, ActorEvent.actorSuspend.eventName)
    }

    @Test
    fun suspendがfalseになったときactorUnsuspendイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""), suspend = true)

        actor.suspend = false

        assertContainsEvent(actor, ActorEvent.actorUnsuspend.eventName)
    }

    @Test
    fun alsoKnownAsに自分自身が含まれない場合更新される() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        val actorIds = setOf(ActorId(100), ActorId(200))
        actor.alsoKnownAs = actorIds

        assertEquals(actorIds, actor.alsoKnownAs)
    }

    @Test
    fun moveToに自分自身が設定された場合moveイベントが発生し更新される() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))


        actor.moveTo = ActorId(100)

        assertContainsEvent(actor, ActorEvent.move.eventName)
    }

    @Test
    fun alsoKnownAsに自分自身が含まれてはいけない() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        assertThrows<IllegalArgumentException> {
            actor.alsoKnownAs = setOf(actor.id)
        }
    }

    @Test
    fun moveToに自分自身が設定されてはいけない() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        assertThrows<IllegalArgumentException> {
            actor.moveTo = actor.id
        }
    }

    @Test
    fun descriptionが更新されたときupdateイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        actor.description = ActorDescription("hoge fuga")

        assertContainsEvent(actor, ActorEvent.update.eventName)
    }

    @Test
    fun screenNameが更新されたときupdateイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        actor.screenName = ActorScreenName("fuga hoge")

        assertContainsEvent(actor, ActorEvent.update.eventName)
    }

    @Test
    fun deleteが実行されたときすでにdeletedがtrueなら何もしない() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""), deleted = true)

        actor.delete()

        assertEmpty(actor)
    }

    @Test
    fun deleteが実行されたときdeletedがfalseならdeleteイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        actor.delete()

        assertEquals(ActorScreenName.empty, actor.screenName)
        assertEquals(ActorDescription.empty, actor.description)
        assertEquals(emptySet(), actor.emojis)
        assertNull(actor.lastPostAt)
        assertEquals(ActorPostsCount.ZERO, actor.postsCount)
        assertNull(actor.followersCount)
        assertNull(actor.followingCount)
        assertContainsEvent(actor, ActorEvent.delete.eventName)
    }

    @Test
    fun restoreが実行されたときcheckUpdateイベントが発生する() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""), deleted = true)

        actor.restore()

        assertFalse(actor.deleted)
        assertContainsEvent(actor, ActorEvent.checkUpdate.eventName)
    }

    @Test
    fun checkUpdateが実行されたときcheckUpdateイベントがh() {
        val actor = TestActorFactory.create(publicKey = ActorPublicKey(""))

        actor.checkUpdate()

        assertContainsEvent(actor, ActorEvent.checkUpdate.eventName)
    }
}