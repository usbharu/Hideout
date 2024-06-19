package dev.usbharu.hideout.core.domain.service.actor.local

import dev.usbharu.hideout.core.domain.model.actor.ActorId
import dev.usbharu.hideout.core.domain.model.actor.TestActorFactory
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class LocalActorMigrationCheckDomainServiceImplTest {
    @Test
    fun 自分自身に引っ越しできない(): Unit = runTest {

        val from = TestActorFactory.create()
        val to = TestActorFactory.create()

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(from, from)

        assertInstanceOf(AccountMigrationCheck.SelfReferences::class.java, canAccountMigration)
    }

    @Test
    fun 引越し先が引っ越している場合は引っ越しできない(): Unit = runTest {

        val from = TestActorFactory.create()
        val to = TestActorFactory.create(moveTo = 100)

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(from, to)

        assertInstanceOf(AccountMigrationCheck.AlreadyMoved::class.java, canAccountMigration)
    }

    @Test
    fun 自分自身が引っ越している場合は引っ越しできない() = runTest {
        val from = TestActorFactory.create(moveTo = 100)
        val to = TestActorFactory.create()

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(from, to)

        assertInstanceOf(AccountMigrationCheck.AlreadyMoved::class.java, canAccountMigration)
    }

    @Test
    fun 引越し先のalsoKnownAsに引越し元が含まれてない場合失敗する() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create(alsoKnownAs = setOf(ActorId(100)))

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(from, to)

        assertInstanceOf(AccountMigrationCheck.AlsoKnownAsNotFound::class.java, canAccountMigration)
    }

    @Test
    fun 正常に設定されている場合は成功する() = runTest {
        val from = TestActorFactory.create()
        val to = TestActorFactory.create(alsoKnownAs = setOf(from.id, ActorId(100)))

        val localActorMigrationCheckDomainServiceImpl = LocalActorMigrationCheckDomainServiceImpl()

        val canAccountMigration = localActorMigrationCheckDomainServiceImpl.canAccountMigration(from, to)

        assertInstanceOf(AccountMigrationCheck.CanAccountMigration::class.java, canAccountMigration)
    }
}