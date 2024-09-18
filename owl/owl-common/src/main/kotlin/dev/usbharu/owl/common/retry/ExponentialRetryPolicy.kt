package dev.usbharu.owl.common.retry

import java.time.Instant
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * 指数関数的に待機時間が増えるリトライポリシー
 * `firstRetrySeconds x attempt ^ 2 - firstRetrySeconds`
 *
 * @property firstRetrySeconds
 */
class ExponentialRetryPolicy(private val firstRetrySeconds: Int = 30) : RetryPolicy {
    override fun nextRetry(now: Instant, attempt: Int): Instant =
        now.plusSeconds(firstRetrySeconds.times((2.0).pow(attempt).roundToLong()) - firstRetrySeconds)
}
