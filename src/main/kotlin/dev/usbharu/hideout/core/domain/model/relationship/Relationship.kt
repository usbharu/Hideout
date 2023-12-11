package dev.usbharu.hideout.core.domain.model.relationship

/**
 * ユーザーとの関係を表します
 *
 * @property userId ユーザー
 * @property targetUserId 相手ユーザー
 * @property following フォローしているか
 * @property blocking ブロックしているか
 * @property muting ミュートしているか
 * @property followRequest フォローリクエストを送っているか
 * @property ignoreFollowRequestFromTarget フォローリクエストを無視しているか
 */
data class Relationship(
    val userId: Long,
    val targetUserId: Long,
    val following: Boolean,
    val blocking: Boolean,
    val muting: Boolean,
    val followRequest: Boolean,
    val ignoreFollowRequestFromTarget: Boolean
)
