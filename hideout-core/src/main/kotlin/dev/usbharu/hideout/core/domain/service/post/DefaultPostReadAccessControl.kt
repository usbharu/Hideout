package dev.usbharu.hideout.core.domain.service.post

import dev.usbharu.hideout.core.domain.model.post.Post
import dev.usbharu.hideout.core.domain.model.post.Visibility
import dev.usbharu.hideout.core.domain.model.relationship.Relationship
import dev.usbharu.hideout.core.domain.model.relationship.RelationshipRepository
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.model.support.principal.Principal
import org.springframework.stereotype.Component

interface IPostReadAccessControl {
    suspend fun isAllow(post: Post, principal: Principal): Boolean
    suspend fun areAllows(postList: List<Post>, principal: Principal): List<Post>
}

@Component
class DefaultPostReadAccessControl(private val relationshipRepository: RelationshipRepository) :
    IPostReadAccessControl {
    override suspend fun isAllow(post: Post, principal: Principal): Boolean {
        // ポスト主は無条件で見れる
        if (post.actorId == principal.actorId) {
            return true
        }

        val relationship = (
                relationshipRepository.findByActorIdAndTargetId(post.actorId, principal.actorId)
                    ?: Relationship.default(post.actorId, principal.actorId)
                )

        // ブロックされてたら見れない
        if (relationship.blocking) {
            return false
        }

        // PublicかUnlistedなら見れる
        if (post.visibility == Visibility.PUBLIC || post.visibility == Visibility.UNLISTED) {
            return true
        }

        // principalがAnonymousなら見れない
        if (principal is Anonymous) {
            return false
        }

        // DirectでvisibleActorsに含まれていたら見れる
        if (post.visibility == Visibility.DIRECT && post.visibleActors.contains(principal.actorId)) {
            return true
        }

        // Followersでフォロワーなら見れる
        if (post.visibility == Visibility.FOLLOWERS) {
            val inverseRelationship =
                relationshipRepository.findByActorIdAndTargetId(principal.actorId, post.actorId) ?: return false

            return inverseRelationship.following
        }

        // その他の場合は見れない
        return false
    }

    override suspend fun areAllows(postList: List<Post>, principal: Principal): List<Post> {
        val actorIds = postList.map { it.actorId }
        val relationshipList =
            relationshipRepository.findByActorIdsAndTargetIdAndBlocking(actorIds, principal.actorId, true)
                .map { it.actorId }
        val inverseRelationshipList =
            relationshipRepository.findByActorIdAndTargetIdsAndFollowing(principal.actorId, actorIds, true)
                .map { it.actorId }

        fun internalAllow(post: Post): Boolean {
            // ポスト主は無条件で見れる
            if (post.actorId == principal.actorId) {
                return true
            }

            if (relationshipList.contains(post.actorId)) {
                return false
            }

            if (post.visibility == Visibility.PUBLIC || post.visibility == Visibility.UNLISTED) {
                return true
            }

            if (principal is Anonymous) {
                return false
            }

            if (post.visibility == Visibility.DIRECT && post.visibleActors.contains(principal.actorId)) {
                return true
            }

            if (post.visibility == Visibility.FOLLOWERS && inverseRelationshipList.contains(principal.actorId)) {
                return true
            }
            return false
        }

        return postList
            .filter {
                internalAllow(it)
            }
    }
}
