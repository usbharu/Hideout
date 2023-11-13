package util

import org.springframework.core.annotation.AliasFor
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithHttpSignatureSecurityContextFactory::class)
annotation class WithHttpSignature(
    @get:AliasFor(
        annotation = WithSecurityContext::class
    ) val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_METHOD,
    val keyId: String = "https://example.com/users/test-user#pubkey",
    val url: String = "https://example.com/inbox",
    val method: String = "GET"
)
