package dev.usbharu.hideout.activitypub.service.activity.accept

import dev.usbharu.hideout.activitypub.domain.exception.IllegalActivityPubObjectException
import dev.usbharu.hideout.activitypub.domain.model.Accept
import dev.usbharu.hideout.activitypub.domain.model.Follow
import dev.usbharu.hideout.activitypub.domain.model.Like
import dev.usbharu.hideout.activitypub.service.common.ActivityPubProcessContext
import dev.usbharu.hideout.activitypub.service.common.ActivityType
import dev.usbharu.hideout.application.config.ActivityPubConfig
import dev.usbharu.hideout.core.query.ActorQueryService
import dev.usbharu.hideout.core.service.relationship.RelationshipService
import dev.usbharu.httpsignature.common.HttpHeaders
import dev.usbharu.httpsignature.common.HttpMethod
import dev.usbharu.httpsignature.common.HttpRequest
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import utils.TestTransaction
import utils.UserBuilder
import java.net.URL


@ExtendWith(MockitoExtension::class)
class ApAcceptProcessorTest {

    @Mock
    private lateinit var actorQueryService: ActorQueryService

    @Mock
    private lateinit var relationshipService: RelationshipService

    @Spy
    private val transaction = TestTransaction

    @InjectMocks
    private lateinit var apAcceptProcessor: ApAcceptProcessor

    @Test
    fun `internalProcess objectがFollowの場合フォローを承認する`() = runTest {

        val json = """"""
        val objectMapper = ActivityPubConfig().objectMapper()
        val jsonNode = objectMapper.readTree(json)

        val accept = Accept(
            apObject = Follow(
                apObject = "https://example.com",
                actor = "https://remote.example.com"
            ),
            actor = "https://example.com"
        )
        val activity = ActivityPubProcessContext<Accept>(
            accept, jsonNode, HttpRequest(
                URL("https://example.com"),
                HttpHeaders(emptyMap()), HttpMethod.POST
            ), null, true
        )

        val user = UserBuilder.localUserOf()
        whenever(actorQueryService.findByUrl(eq("https://example.com"))).doReturn(user)
        val remoteUser = UserBuilder.remoteUserOf()
        whenever(actorQueryService.findByUrl(eq("https://remote.example.com"))).doReturn(remoteUser)

        apAcceptProcessor.internalProcess(activity)

        verify(relationshipService, times(1)).acceptFollowRequest(eq(user.id), eq(remoteUser.id), eq(false))
    }

    @Test
    fun `internalProcess objectがFollow以外の場合IllegalActivityPubObjecExceptionが発生する`() = runTest {
        val json = """"""
        val objectMapper = ActivityPubConfig().objectMapper()
        val jsonNode = objectMapper.readTree(json)

        val accept = Accept(
            apObject = Like(
                apObject = "https://example.com",
                actor = "https://remote.example.com",
                content = "",
                id = ""
            ),
            actor = "https://example.com"
        )
        val activity = ActivityPubProcessContext<Accept>(
            accept, jsonNode, HttpRequest(
                URL("https://example.com"),
                HttpHeaders(emptyMap()), HttpMethod.POST
            ), null, true
        )

        assertThrows<IllegalActivityPubObjectException> {
            apAcceptProcessor.internalProcess(activity)
        }
    }

    @Test
    fun `isSupproted Acceptにはtrue`() {
        val actual = apAcceptProcessor.isSupported(ActivityType.Accept)
        assertThat(actual).isTrue()
    }

    @TestFactory
    fun `isSupported Accept以外にはfalse`(): List<DynamicTest> {
        return ActivityType
            .values()
            .filterNot { it == ActivityType.Accept }
            .map {
                dynamicTest("isSupported $it にはfalse") {

                    val actual = apAcceptProcessor.isSupported(it)
                    assertThat(actual).isFalse()
                }
            }
    }

    @Test
    fun `type Acceptのclassjavaが返ってくる`() {
        assertThat(apAcceptProcessor.type()).isEqualTo(Accept::class.java)
    }
}
