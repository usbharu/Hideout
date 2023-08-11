package dev.usbharu.hideout.query

import dev.usbharu.hideout.domain.model.hideout.entity.Post
import dev.usbharu.hideout.repository.Posts
import dev.usbharu.hideout.repository.toPost
import org.jetbrains.exposed.sql.select
import org.koin.core.annotation.Single

@Single
class PostQueryServiceImpl : PostQueryService {
    override suspend fun findById(id: Long): Post = Posts.select { Posts.id eq id }.single().toPost()

    override suspend fun findByUrl(url: String): Post = Posts.select { Posts.url eq url }.single().toPost()

    override suspend fun findByApId(string: String): Post = Posts.select { Posts.apId eq string }.single().toPost()
}
