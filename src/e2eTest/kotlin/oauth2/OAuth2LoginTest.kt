package oauth2

import dev.usbharu.hideout.SpringApplication
import org.jsoup.Jsoup
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@SpringBootTest(
    classes = [SpringApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestMethodOrder(OrderAnnotation::class)
class OAuth2LoginTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    @Order(2)
    fun アカウント作成() {
        val returnResult = webTestClient.get()
            .uri("/auth/sign_up")
            .exchange()
            .expectStatus()
            .isOk
            .returnResult(String::class.java)

        val html = returnResult
            .responseBody
            .toStream()
            .toList()
            .toList()
            .joinToString("")

        val session = returnResult.responseCookies["JSESSIONID"]?.first()?.value!!

        val attr = Jsoup.parse(html).selectXpath("//input[@name=\"_csrf\"]").attr("value")

        println("CSRF TOKEN = $attr")

        webTestClient
            .post()
            .uri("/api/v1/accounts")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("username", "oatuh-login-test")
                    .with("password", "very-secure-password").with("_csrf", attr)
            )
            .cookie("JSESSIONID", session)
            .exchange()
            .expectStatus().isFound
            .expectCookie()

    }

//    @Test
//    fun `OAuth2で権限read writeを持ったトークンでのログインができる`() {
////        webTestClient.post().uri("/api/v1/apps")
//    }
}
