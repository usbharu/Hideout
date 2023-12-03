import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

object AssertionUtil {

    fun assertUserExist(username: String, domain: String): Boolean {
        val selectAll = Users.selectAll()
        println(selectAll.fetchSize)

        println(selectAll.toList().size)

        selectAll.map { "@${it[Users.name]}@${it[Users.domain]}" }.forEach { println(it) }

        return Users.select { Users.name eq username and (Users.domain eq domain) }.empty().not()
    }
}
