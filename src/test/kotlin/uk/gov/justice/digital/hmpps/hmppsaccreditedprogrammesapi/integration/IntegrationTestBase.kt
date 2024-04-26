package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.TestPropertiesInitializer
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.PersistenceHelper
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.Duration
import java.util.*

object WiremockPortHolder {
  private val possiblePorts = 57830..57880

  private var port: Int? = null
  private var channel: FileChannel? = null

  fun getPort(): Int {
    synchronized(this) {
      if (port != null) {
        return port!!
      }

      possiblePorts.forEach { portToTry ->
        val lockFilePath =
          Paths.get("${System.getProperty("java.io.tmpdir")}${System.getProperty("file.separator")}ap-int-port-lock-$portToTry.lock")

        try {
          channel = FileChannel.open(lockFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
          channel!!.position(0)

          if (channel!!.tryLock() == null) {
            channel!!.close()
            channel = null
            return@forEach
          }

          port = portToTry

          return portToTry
        } catch (_: Exception) {
        }
      }

      throw RuntimeException("Could not lock any potential Wiremock ports")
    }
  }

  fun releasePort() = channel?.close()
}

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
@ContextConfiguration(initializers = [TestPropertiesInitializer::class])
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  lateinit var wiremockServer: WireMockServer

  @Autowired
  lateinit var persistenceHelper: PersistenceHelper

  @Autowired
  lateinit var hmppsQueueService: HmppsQueueService

  val domainEventQueue by lazy {
    hmppsQueueService.findByQueueId("hmppsdomaineventsqueue")
      ?: throw MissingQueueException("HmppsQueue hmppsdomaineventsqueue not found")
  }
  val domainEventQueueDlqClient by lazy { domainEventQueue.sqsDlqClient }
  val domainEventQueueClient by lazy { domainEventQueue.sqsClient }

  @Value("\${wiremock.port}")
  var wiremockPort: Int = 0

  val objectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
  }

  @BeforeEach
  fun beforeEach() {
    domainEventQueueClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.queueUrl).build()).get()
    domainEventQueueDlqClient!!.purgeQueue(PurgeQueueRequest.builder().queueUrl(domainEventQueue.dlqUrl).build())
      .get()

    webTestClient.mutate()
      .responseTimeout(Duration.ofMillis(30000))
      .build()

    wiremockServer = WireMockServer(
      WireMockConfiguration()
        .port(wiremockPort)
        .usingFilesUnderClasspath("simulations")
        .maxLoggedResponseSize(100_000),
    )
    wiremockServer.start()
  }

  @AfterEach
  fun stopMockServer() {
    wiremockServer.stop()
  }

  fun mockClientCredentialsJwtRequest(
    username: String? = null,
    roles: List<String> = listOf(),
    authSource: String = "none",
    jwt: String = "token",
  ) {
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
        .willReturn(
          WireMock.aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(
                GetTokenResponse(
                  accessToken = jwt,
                  tokenType = "bearer",
                  expiresIn = Duration.ofHours(1).toSeconds().toInt(),
                  scope = "read",
                  sub = username?.uppercase() ?: "integration-test-client-id",
                  authSource = authSource,
                  jti = UUID.randomUUID().toString(),
                  iss = "http://localhost:9092/auth/issuer",
                ),
              ),
            ),
        ),
    )
  }

  data class GetTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    val scope: String,
    val sub: String,
    @JsonProperty("auth_source")
    val authSource: String,
    val jti: String,
    val iss: String,
  )

  fun getAllCourses(): List<Course> =
    webTestClient
      .get()
      .uri("/courses")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<Course>>()
      .returnResult().responseBody!!

  fun getAllOfferingsForCourse(courseId: UUID): List<CourseOffering> =
    webTestClient
      .get()
      .uri("/courses/$courseId/offerings")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<CourseOffering>>()
      .returnResult().responseBody!!
}
