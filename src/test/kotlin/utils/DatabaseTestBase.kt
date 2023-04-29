package utils

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(DBResetInterceptor::class)
abstract class DatabaseTestBase {
    companion object {
        init {
            Database.connect(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver",
                databaseConfig = DatabaseConfig { useNestedTransactions = true }
            )
        }
    }
}
