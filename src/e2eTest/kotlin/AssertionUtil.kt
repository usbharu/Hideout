import dev.usbharu.hideout.core.infrastructure.exposedrepository.Users
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.net.MalformedURLException
import java.net.URL

object AssertionUtil {

    @JvmStatic
    fun assertUserExist(username: String, domain: String): Boolean {
        val s = try {
            val url = URL(domain)
            url.host + ":" + url.port.toString().takeIf { it != "-1" }.orEmpty()
        } catch (e: MalformedURLException) {
            domain
        }

        val selectAll = Users.selectAll()
        println(selectAll.fetchSize)

        println(selectAll.toList().size)

        selectAll.map { "@${it[Users.name]}@${it[Users.domain]}" }.forEach { println(it) }

        return Users.select { Users.name eq username and (Users.domain eq s) }.empty().not()
    }
}
