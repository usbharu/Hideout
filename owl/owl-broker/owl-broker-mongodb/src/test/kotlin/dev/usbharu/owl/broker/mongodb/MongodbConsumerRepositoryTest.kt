package dev.usbharu.owl.broker.mongodb

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.usbharu.owl.broker.domain.model.consumer.Consumer
import kotlinx.coroutines.runBlocking
import org.bson.UuidRepresentation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.*


class MongodbConsumerRepositoryTest {
    @Test
    @Disabled
    fun name() {

        val clientSettings =
            MongoClientSettings.builder().applyConnectionString(ConnectionString("mongodb://localhost:27017"))
                .uuidRepresentation(UuidRepresentation.STANDARD).build()


        val database = MongoClient.create(clientSettings).getDatabase("mongo-test")

        val mongodbConsumerRepository = MongodbConsumerRepository(database)

        val consumer = Consumer(
            UUID.randomUUID(),
            name = "test",
            hostname = "aaa",
            tasks = listOf("a", "b", "c")
        )
        runBlocking {
            mongodbConsumerRepository.save(consumer)

            val findById = mongodbConsumerRepository.findById(UUID.randomUUID())
            assertEquals(null, findById)

            val findById1 = mongodbConsumerRepository.findById(consumer.id)
            assertEquals(consumer, findById1)

            mongodbConsumerRepository.save(consumer.copy(name = "test2"))

            val findById2 = mongodbConsumerRepository.findById(consumer.id)
            assertEquals(consumer.copy(name = "test2"), findById2)
        }
    }
}