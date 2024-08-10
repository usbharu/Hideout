package dev.usbharu.hideout.core.application.post

import dev.usbharu.hideout.core.application.exception.PermissionDeniedException
import dev.usbharu.hideout.core.domain.model.post.PostId
import dev.usbharu.hideout.core.domain.model.post.PostRepository
import dev.usbharu.hideout.core.domain.model.post.TestPostFactory
import dev.usbharu.hideout.core.domain.model.support.principal.Anonymous
import dev.usbharu.hideout.core.domain.service.post.IPostReadAccessControl
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import utils.TestTransaction

@ExtendWith(MockitoExtension::class)
class GetPostApplicationServiceTest {
    @InjectMocks
    lateinit var service: GetPostApplicationService

    @Mock
    lateinit var postRepository: PostRepository

    @Mock
    lateinit var iPostReadAccessControl: IPostReadAccessControl

    @Spy
    val transaction = TestTransaction

    @Test
    fun postReadAccessControlがtrueを返したらPostが返ってくる() = runTest {
        val post = TestPostFactory.create(id = 1)
        whenever(postRepository.findById(PostId(1))).doReturn(post)
        whenever(iPostReadAccessControl.isAllow(any(), any())).doReturn(true)

        val actual = service.execute(GetPost(1), Anonymous)
        assertEquals(Post.of(post), actual)
    }

    @Test
    fun postが見つからない場合失敗() = runTest {
        assertThrows<IllegalArgumentException> {
            service.execute(GetPost(2), Anonymous)
        }
    }

    @Test
    fun postReadAccessControlがfalseを返したら失敗() = runTest {
        val post = TestPostFactory.create(id = 1)
        whenever(postRepository.findById(PostId(1))).doReturn(post)
        whenever(iPostReadAccessControl.isAllow(any(), any())).doReturn(false)
        assertThrows<PermissionDeniedException> {
            service.execute(GetPost(1), Anonymous)
        }

    }
}