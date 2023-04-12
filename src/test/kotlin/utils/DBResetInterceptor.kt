package utils

import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.extension.*

class DBResetInterceptor : BeforeAllCallback,AfterAllCallback,BeforeEachCallback,AfterEachCallback {
    private lateinit var transactionAll: Transaction
    private lateinit var transactionEach: Transaction

    override fun beforeAll(context: ExtensionContext?) {
        transactionAll = TransactionManager.manager.newTransaction()
    }

    override fun afterAll(context: ExtensionContext?) {
        transactionAll.rollback()
        transactionAll.close()
    }

    override fun beforeEach(context: ExtensionContext?) {
        transactionEach = TransactionManager.manager.newTransaction(outerTransaction = transactionAll)
    }

    override fun afterEach(context: ExtensionContext?) {
        transactionEach.rollback()
        transactionEach.close()
    }
}
