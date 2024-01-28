package dev.usbharu.hideout.mastodon.domain.model

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationTypeTest {
    @ParameterizedTest
    @MethodSource("parseSuccessProvider")
    fun parseに成功する(s: String, notificationType: NotificationType) {
        assertEquals(notificationType, NotificationType.parse(s))
    }

    @ParameterizedTest
    @ValueSource(strings = ["hoge", "fuga", "0x1234", "follow_reject", "test", "mentiooon", "emoji_reaction", "reaction"])
    fun parseに失敗する(s: String) {
        assertNull(NotificationType.parse(s))
    }

    companion object {
        @JvmStatic
        fun parseSuccessProvider(): Stream<Arguments> {
            return Stream.of(
                arguments("mention", NotificationType.mention),
                arguments("status", NotificationType.status),
                arguments("reblog", NotificationType.reblog),
                arguments("follow", NotificationType.follow),
                arguments("follow_request", NotificationType.follow_request),
                arguments("favourite", NotificationType.favourite),
                arguments("poll", NotificationType.poll),
                arguments("update", NotificationType.update),
                arguments("admin.sign_up", NotificationType.admin_sign_up),
                arguments("admin.report", NotificationType.admin_report),
                arguments("servered_relationships", NotificationType.severed_relationships)
            )
        }
    }
}
