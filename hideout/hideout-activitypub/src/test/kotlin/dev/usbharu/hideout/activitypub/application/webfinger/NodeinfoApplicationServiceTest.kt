package dev.usbharu.hideout.activitypub.application.webfinger


import dev.usbharu.hideout.activitypub.application.nodeinfo.NodeinfoApplicationService
import dev.usbharu.hideout.activitypub.application.nodeinfo.NodeinfoRequest
import dev.usbharu.hideout.core.config.ApplicationConfig
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.info.BuildProperties
import util.TestTransaction
import java.net.URI
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExtendWith(MockitoExtension::class)
class NodeinfoApplicationServiceTest {
    @InjectMocks
    lateinit var nodeinfoApplicationService: NodeinfoApplicationService

    @Spy
    val buildInfo: BuildProperties = BuildProperties(Properties())

    @Spy
    val applicationConfig = ApplicationConfig(URI.create("https://example.com"))

    @Spy
    val transaction = TestTransaction

    @Test
    fun nodeinfo2_0() = runTest {
        val execute = nodeinfoApplicationService.execute(NodeinfoRequest("2.0"), Anonymous)

        assertFalse(execute.openRegistration)
        assertEquals("2.0", execute.version)
        assertEquals("hideout", execute.software["name"])
    }

    @Test
    fun nodeinfo2_1() = runTest {
        val execute = nodeinfoApplicationService.execute(NodeinfoRequest("2.1"), Anonymous)

        assertFalse(execute.openRegistration)
        assertEquals("2.1", execute.version)
        assertEquals("hideout", execute.software["name"])
    }
}