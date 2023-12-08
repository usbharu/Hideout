package dev.usbharu.hideout.core.infrastructure.exposedrepository

import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.block.Block
import dev.usbharu.hideout.core.domain.model.block.BlockRepository
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository

@Repository
class BlockRepositoryImpl : BlockRepository {
    override suspend fun save(block: Block): Block {
        Blocks.insert {
            it[userId] = block.userId
            it[target] = block.target
        }
        return block
    }

    override suspend fun delete(block: Block) {
        Blocks.deleteWhere { Blocks.userId eq block.userId and (Blocks.target eq block.target) }
    }

    override suspend fun findByUserIdAndTarget(userId: Long, target: Long): Block {
        val singleOr = Blocks
            .select { Blocks.userId eq userId and (Blocks.target eq target) }
            .singleOr {
                FailedToGetResourcesException(
                    "userId: $userId target: $target is duplicate or not exist.",
                    it
                )
            }

        return Block(
            singleOr[Blocks.userId],
            singleOr[Blocks.target]
        )
    }
}

object Blocks : LongIdTable("blocks") {
    val userId = long("user_id").references(Users.id).index()
    val target = long("target").references(Users.id)

    init {
        uniqueIndex(userId, target)
    }
}
