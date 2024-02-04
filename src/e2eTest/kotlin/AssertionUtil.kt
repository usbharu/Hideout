import dev.usbharu.hideout.core.infrastructure.exposedrepository.Actors
import org.jetbrains.exposed.sql.and
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

        val selectAll = Actors.selectAll()
        println(selectAll.fetchSize)

        println(selectAll.toList().size)

        selectAll.map { "@${it[Actors.name]}@${it[Actors.domain]}" }.forEach { println(it) }

        return Actors.selectAll().where { Actors.name eq username and (Actors.domain eq s) }.empty().not()
    }
}
