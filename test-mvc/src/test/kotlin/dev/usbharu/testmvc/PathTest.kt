package dev.usbharu.testmvc

import dev.usbharu.testmvc.path.Path
import dev.usbharu.testmvc.path.StringListParameter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PathTest {
    @Test
    fun `build url`() {
        val buildUrls = Path("a", "b", StringListParameter(listOf("c", "d"))).buildUrls().sorted()

        assertEquals(listOf("/a/b/c", "/a/b/d").sorted(), buildUrls)
    }

    @Test
    fun `build url2`() {
        val buildUrls =
            Path(StringListParameter(listOf("a", "e")), "b", StringListParameter(listOf("c", "d"))).buildUrls().sorted()

        assertEquals(listOf("/a/b/c", "/e/b/c", "/a/b/d", "/e/b/d").sorted(), buildUrls)
    }

    @Test
    fun `build url3`() {
        val buildUrls = Path("a", "b", "c").buildUrls().sorted()

        assertEquals(listOf("/a/b/c").sorted(), buildUrls)
    }
}