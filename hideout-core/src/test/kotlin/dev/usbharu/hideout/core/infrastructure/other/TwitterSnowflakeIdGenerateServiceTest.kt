package dev.usbharu.hideout.core.infrastructure.other

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TwitterSnowflakeIdGenerateServiceTest {
    @Test
    fun noDuplicateTest() = runBlocking {
        val mutex = Mutex()
        val mutableListOf = mutableListOf<Long>()
        coroutineScope {
            repeat(500000) {
                launch(Dispatchers.IO) {
                    val id = TwitterSnowflakeIdGenerateService.generateId()
                    mutex.withLock {
                        mutableListOf.add(id)
                    }
                }
            }
        }

        assertEquals(0, mutableListOf.size - mutableListOf.toSet().size)
    }
}