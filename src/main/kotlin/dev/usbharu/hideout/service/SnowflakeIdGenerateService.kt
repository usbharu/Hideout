package dev.usbharu.hideout.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

open class SnowflakeIdGenerateService(private val baseTime:Long) : IdGenerateService {
    var lastTimeStamp: Long = -1
    var sequenceId: Int = 0
    val mutex = Mutex()

    @Throws(IllegalStateException::class)
    override suspend fun generateId(): Long {
        return mutex.withLock {

            var timestamp = getTime()
            if (timestamp < lastTimeStamp) {
                while (timestamp <= lastTimeStamp) {
                    delay(1L)
                    timestamp = getTime()
                }
                //            throw IllegalStateException(" $lastTimeStamp $timestamp ${lastTimeStamp-timestamp}  ")
            }
            if (timestamp == lastTimeStamp) {
                sequenceId++
                if (sequenceId >= 4096) {
                    while (timestamp <= lastTimeStamp) {
                        delay(1L)
                        timestamp = getTime()
                    }
                    sequenceId = 0
                }
            } else {
                sequenceId = 0
            }
            lastTimeStamp = timestamp
            return@withLock (timestamp - baseTime).shl(22).or(1L.shl(12)).or(sequenceId.toLong())
        }


    }

    private fun getTime(): Long {
        return Instant.now().toEpochMilli()
    }
}
