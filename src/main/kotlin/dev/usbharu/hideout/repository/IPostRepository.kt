package dev.usbharu.hideout.repository

import dev.usbharu.hideout.domain.model.Post
import dev.usbharu.hideout.domain.model.PostEntity

interface IPostRepository {
    suspend fun insert(post:Post):PostEntity
    suspend fun findOneById(id:Long):PostEntity
    suspend fun delete(id:Long)
}
