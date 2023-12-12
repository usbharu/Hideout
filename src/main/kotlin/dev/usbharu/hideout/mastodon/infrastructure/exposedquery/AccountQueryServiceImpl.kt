package dev.usbharu.hideout.mastodon.infrastructure.exposedquery

import dev.usbharu.hideout.application.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.exception.FailedToGetResourcesException
import dev.usbharu.hideout.core.domain.model.relationship.Relationships
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import dev.usbharu.hideout.core.infrastructure.exposedrepository.Posts
import dev.usbharu.hideout.domain.mastodon.model.generated.Account
import dev.usbharu.hideout.mastodon.query.AccountQueryService
import dev.usbharu.hideout.util.singleOr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class AccountQueryServiceImpl(private val applicationConfig: ApplicationConfig) : AccountQueryService {
    override suspend fun findById(accountId: Long): Account {
        val followingCount = Count(Relationships.actorId.eq(Actors.id), true).alias("following_count")
        val followersCount = Count(Relationships.targetActorId.eq(Actors.id), true).alias("followers_count")
        val postsCount = Posts.id.countDistinct().alias("posts_count")
        val lastCreated = Posts.createdAt.max().alias("last_created")
        val query = Actors
            .join(Relationships, JoinType.LEFT) {
                Actors.id eq Relationships.actorId or (Actors.id eq Relationships.targetActorId)
            }
            .leftJoin(Posts)
            .slice(
                followingCount,
                followersCount,
                *(Actors.realFields.toTypedArray()),
                lastCreated,
                postsCount
            )
            .select { Actors.id eq accountId and (Relationships.following eq true or (Relationships.following.isNull())) }
            .groupBy(Actors.id)

        return query
            .singleOr { FailedToGetResourcesException("accountId: $accountId wad not exist or duplicate", it) }
            .let { toAccount(it, followingCount, followersCount, postsCount, lastCreated) }
    }

    override suspend fun findByIds(accountIds: List<Long>): List<Account> {
        val followingCount = Count(Relationships.actorId.eq(Actors.id), true).alias("following_count")
        val followersCount = Count(Relationships.targetActorId.eq(Actors.id), true).alias("followers_count")
        val postsCount = Posts.id.countDistinct().alias("posts_count")
        val lastCreated = Posts.createdAt.max().alias("last_created")
        val query = Actors
            .join(Relationships, JoinType.LEFT) {
                Actors.id eq Relationships.actorId or (Actors.id eq Relationships.targetActorId)
            }
            .leftJoin(Posts)
            .slice(
                followingCount,
                followersCount,
                *(Actors.realFields.toTypedArray()),
                lastCreated,
                postsCount
            )
            .select { Actors.id inList accountIds and (Relationships.following eq true or (Relationships.following.isNull())) }
            .groupBy(Actors.id)

        return query
            .map { toAccount(it, followingCount, followersCount, postsCount, lastCreated) }
    }

    private fun toAccount(
        resultRow: ResultRow,
        followingCount: ExpressionAlias<Long>,
        followersCount: ExpressionAlias<Long>,
        postsCount: ExpressionAlias<Long>,
        lastCreated: ExpressionAlias<Long?>
    ): Account {
        val userUrl = "${applicationConfig.url}/users/${resultRow[Actors.id]}"

        return Account(
            id = resultRow[Actors.id].toString(),
            username = resultRow[Actors.name],
            acct = "${resultRow[Actors.name]}@${resultRow[Actors.domain]}",
            url = resultRow[Actors.url],
            displayName = resultRow[Actors.screenName],
            note = resultRow[Actors.description],
            avatar = userUrl + "/icon.jpg",
            avatarStatic = userUrl + "/icon.jpg",
            header = userUrl + "/header.jpg",
            headerStatic = userUrl + "/header.jpg",
            locked = resultRow[Actors.locked],
            fields = emptyList(),
            emojis = emptyList(),
            bot = false,
            group = false,
            discoverable = true,
            createdAt = Instant.ofEpochMilli(resultRow[Actors.createdAt]).toString(),
            lastStatusAt = resultRow[lastCreated]?.let { Instant.ofEpochMilli(it).toString() },
            statusesCount = resultRow[postsCount].toInt(),
            followersCount = resultRow[followersCount].toInt(),
            followingCount = resultRow[followingCount].toInt(),
        )
    }
}
