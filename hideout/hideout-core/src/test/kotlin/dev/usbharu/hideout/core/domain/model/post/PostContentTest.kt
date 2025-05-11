package dev.usbharu.hideout.core.domain.model.post

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PostContentTest {
    @Test
    fun textがtext_lengthを超える場合は切り取られてwasTruncatedがtrue() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH + 1),
            "b".repeat(PostContent.CONTENT_LENGTH),
            emptyList()
        )

        assertTrue(postContent.wasTruncated)

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }

    @Test
    fun textがtext_lengthを超えない場合は変わらずwasTruncatedがfalse() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH),
            "b".repeat(PostContent.CONTENT_LENGTH),
            emptyList()
        )

        assertFalse(postContent.wasTruncated)

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }

    @Test
    fun contentがcontent_lengthを超える場合は切り取られてwasTruncatedがtrue() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH),
            "b".repeat(PostContent.CONTENT_LENGTH + 1),
            emptyList()
        )

        assertTrue(postContent.wasTruncated)

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }
}