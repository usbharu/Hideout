package dev.usbharu.hideout.service.ap

import dev.usbharu.hideout.exception.JsonParseException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import utils.JsonObjectMapper.objectMapper
import kotlin.test.assertEquals

class APServiceImplTest {
    @Test
    fun `parseActivity 正常なActivityをパースできる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": "Follow"}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity Typeが配列のActivityをパースできる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["Follow"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity Typeが配列で関係ない物が入っていてもパースできる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["Hello","Follow"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity jsonとして解釈できない場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""hoge""")
        }
    }

    @Test
    fun `parseActivity 空の場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("")
        }
    }

    @Test
    fun `parseActivity jsonにtypeプロパティがない場合JsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""{"actor": "https://example.com"}""")
        }
    }

    @Test
    fun `parseActivity typeが配列でないときtypeが未定義の場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": "Hoge"}""")
        }
    }

    @Test
    fun `parseActivity typeが配列のとき定義済みのtypeを見つけられなかった場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": ["Hoge","Fuga"]}""")
        }
    }

    @Test
    fun `parseActivity typeが空の場合IllegalArgumentExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity("""{"type": ""}""")
        }
    }

    @Test
    fun `parseActivity typeに指定されている文字の判定がcase-insensitiveで行われる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": "FoLlOw"}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity typeが配列のとき指定されている文字の判定がcase-insensitiveで行われる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        val activityType = apServiceImpl.parseActivity("""{"type": ["HoGE","fOllOw"]}""")

        assertEquals(ActivityType.Follow, activityType)
    }

    @Test
    fun `parseActivity activityがarrayのときJsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<JsonParseException> {
            apServiceImpl.parseActivity("""[{"type": "Follow"},{"type": "Accept"}]""")
        }
    }

    @Test
    fun `parseActivity activityがvalueのときJsonParseExceptionがthrowされる`() {
        val apServiceImpl = APServiceImpl(
            apReceiveFollowService = mock(),
            apUndoService = mock(),
            apAcceptService = mock(),
            apCreateService = mock(),
            apLikeService = mock(),
            objectMapper = objectMapper,
            apReceiveFollowJobService = mock(),
            apNoteJobService = mock(),
            apReactionJobService = mock()
        )

        //language=JSON
        assertThrows<IllegalArgumentException> {
            apServiceImpl.parseActivity(""""hoge"""")
        }
    }
}
