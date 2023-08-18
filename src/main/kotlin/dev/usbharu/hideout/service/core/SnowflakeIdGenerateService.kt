package dev.usbharu.hideout.service.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.time.Instant

@Suppress("MagicNumber")
@Service
open class SnowflakeIdGenerateService(private val baseTime: Long) : IdGenerateService {
    var lastTimeStamp: Long = -1
    var sequenceId: Int = 0
    val mutex = Mutex()

    @Throws(IllegalStateException::class)
    override suspend fun generateId(): Long {
        return mutex.withLock {
            var timestamp = getTime()
            if (timestamp < lastTimeStamp) {
                timestamp = wait(timestamp)
                //            throw IllegalStateException(" $lastTimeStamp $timestamp ${lastTimeStamp-timestamp}  ")
            }
            if (timestamp == lastTimeStamp) {
                sequenceId++
                if (sequenceId >= 4096) {
                    timestamp = wait(timestamp)
                    sequenceId = 0
                }
            } else {
                sequenceId = 0
            }
            lastTimeStamp = timestamp
            return@withLock (timestamp - baseTime).shl(22).or(1L.shl(12)).or(sequenceId.toLong())
        }
    }

    private suspend fun wait(timestamp: Long): Long {
        var timestamp1 = timestamp
        while (timestamp1 <= lastTimeStamp) {
            delay(1L)
            timestamp1 = getTime()
        }
        return timestamp1
    }

    private fun getTime(): Long = Instant.now().toEpochMilli()
}
