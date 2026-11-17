package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.Scenario
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

class BaseHMPPSClientRetryTest {

  private lateinit var wireMock: WireMockServer

  private data class Payload(val value: String)

  /**
   * Minimal concrete client over the WireMock server. The WebClient is given a short response
   * timeout to mirror production ([WebClientConfiguration]) so the read-timeout path can be
   * exercised, and the retry backoff is shortened to keep the tests fast.
   */
  private class TestClient(baseUrl: String) :
    BaseHMPPSClient(
      WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(
          ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofMillis(300))),
        )
        .build(),
      jacksonObjectMapper(),
    ) {
    override val retryBackoff: Duration = Duration.ofMillis(10)

    fun getPayload() = getRequest<Payload>("Test Service") { path = "/payload" }

    fun postPayload() = postRequest<Payload>("Test Service") { path = "/payload" }
  }

  @BeforeEach
  fun setUp() {
    wireMock = WireMockServer(wireMockConfig().dynamicPort())
    wireMock.start()
  }

  @AfterEach
  fun tearDown() {
    wireMock.stop()
  }

  private fun client() = TestClient("http://localhost:${wireMock.port()}")

  @Test
  fun `GET recovers after a single transient connection drop`() {
    val scenario = "transient"
    wireMock.stubFor(
      get(urlEqualTo("/payload")).inScenario(scenario)
        .whenScenarioStateIs(Scenario.STARTED)
        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
        .willSetStateTo("recovered"),
    )
    wireMock.stubFor(
      get(urlEqualTo("/payload")).inScenario(scenario)
        .whenScenarioStateIs("recovered")
        .willReturn(
          aResponse().withStatus(200).withHeader("Content-Type", "application/json")
            .withBody("""{"value":"ok"}"""),
        ),
    )

    val result = client().getPayload()

    result.shouldBeInstanceOf<ClientResult.Success<Payload>>()
    result.body.value shouldBe "ok"
    wireMock.verify(2, getRequestedFor(urlEqualTo("/payload")))
  }

  @Test
  fun `GET gives up after exhausting the bounded retries`() {
    wireMock.stubFor(
      get(urlEqualTo("/payload"))
        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)),
    )

    val result = client().getPayload()

    result.shouldBeInstanceOf<ClientResult.Failure.Other<Payload>>()
    // 1 initial attempt + maxRetryAttempts (2) retries.
    wireMock.verify(3, getRequestedFor(urlEqualTo("/payload")))
  }

  @Test
  fun `GET does not retry on a genuine 4xx response`() {
    wireMock.stubFor(
      get(urlEqualTo("/payload")).willReturn(aResponse().withStatus(400)),
    )

    val result = client().getPayload()

    result.shouldBeInstanceOf<ClientResult.Failure.StatusCode<Payload>>()
    result.status.value() shouldBe 400
    wireMock.verify(1, getRequestedFor(urlEqualTo("/payload")))
  }

  @Test
  fun `GET does not retry on a read timeout - a slow upstream must not stack with the caller's own retries`() {
    // A read timeout burns the full upstream timeout; retrying would exceed the UI's deadline and
    // multiply load. Only one attempt is expected.
    wireMock.stubFor(
      get(urlEqualTo("/payload")).willReturn(
        aResponse().withStatus(200).withHeader("Content-Type", "application/json")
          .withBody("""{"value":"ok"}""").withFixedDelay(1000),
      ),
    )

    val result = client().getPayload()

    result.shouldBeInstanceOf<ClientResult.Failure.Other<Payload>>()
    wireMock.verify(1, getRequestedFor(urlEqualTo("/payload")))
  }

  @Test
  fun `non-GET requests are not retried on transient failures`() {
    wireMock.stubFor(
      post(urlEqualTo("/payload"))
        .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)),
    )

    val result = client().postPayload()

    result.shouldBeInstanceOf<ClientResult.Failure.Other<Payload>>()
    result.toException().message.shouldContain("Test Service")
    wireMock.verify(1, postRequestedFor(urlEqualTo("/payload")))
  }
}
