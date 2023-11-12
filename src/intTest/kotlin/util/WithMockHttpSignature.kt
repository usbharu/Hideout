package util

import org.springframework.core.annotation.AliasFor
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockHttpSignatureSecurityContextFactory::class)
annotation class WithMockHttpSignature(
    @get:AliasFor(
        annotation = WithSecurityContext::class
    ) val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD,
    val username: String = "test-user",
    val domain: String = "example.com",
    val keyId: String = "https://example.com/users/test-user#pubkey",
    val id: Long = 1234L,
    val url: String = "https://example.com/inbox",
    val method: String = "GET"
)
