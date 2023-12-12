package dev.usbharu.hideout.core.domain.model.relationship

/**
 * ユーザーとの関係を表します
 *
 * @property actorId ユーザー
 * @property targetActorId 相手ユーザー
 * @property following フォローしているか
 * @property blocking ブロックしているか
 * @property muting ミュートしているか
 * @property followRequest フォローリクエストを送っているか
 * @property ignoreFollowRequestToTarget フォローリクエストを無視しているか
 */
data class Relationship(
    val actorId: Long,
    val targetActorId: Long,
    val following: Boolean,
    val blocking: Boolean,
    val muting: Boolean,
    val followRequest: Boolean,
    val ignoreFollowRequestToTarget: Boolean
)
