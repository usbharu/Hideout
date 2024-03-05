package dev.usbharu.owl.common.retry

import java.time.Instant
import kotlin.math.pow
import kotlin.math.roundToLong

class ExponentialRetryPolicy(private val firstRetrySeconds: Int = 30) : RetryPolicy {
    override fun nextRetry(now: Instant, attempt: Int): Instant =
        now.plusSeconds(firstRetrySeconds.toDouble().pow(attempt + 1.0).roundToLong())

}