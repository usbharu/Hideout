package dev.usbharu.hideout.core.domain.model.post

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PostContentTest {
    @Test
    fun textがtext_lengthを超える場合は切り取られる() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH + 1),
            "b".repeat(PostContent.CONTENT_LENGTH),
            emptyList()
        )

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }

    @Test
    fun contentがcontent_lengthを超える場合は切り取られる() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH),
            "b".repeat(PostContent.CONTENT_LENGTH + 1),
            emptyList()
        )

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }

    @Test
    fun textとcontentがtext_lengthとcontent_lengthを超えない場合は変わらない() {
        val postContent = PostContent(
            "a".repeat(PostContent.TEXT_LENGTH),
            "b".repeat(PostContent.CONTENT_LENGTH),
            emptyList()
        )

        assertEquals(postContent.text, "a".repeat(PostContent.TEXT_LENGTH))
        assertEquals(postContent.content, "b".repeat(PostContent.CONTENT_LENGTH))
    }


}