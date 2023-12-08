package dev.usbharu.hideout.core.domain.model.block

/**
 * ブロック関係を表します
 *
 * @property userId ブロックの動作を行ったユーザーid
 * @property target ブロックの対象のユーザーid
 */
data class Block(
    val userId: Long,
    val target: Long
)
