package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.TestPropertiesInitializer
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.PersistenceHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralUpdate
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

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
@ContextConfiguration(initializers = [TestPropertiesInitializer::class])
abstract class IntegrationTestBase {

  companion object {

    @JvmStatic
    private val postgresContainer = PostgreSQLContainer<Nothing>("postgres:16.4")
      .apply {
        withReuse(true)
      }

    @BeforeAll
    @JvmStatic
    fun startPostgresContainer() {
      postgresContainer.start()
    }

    @DynamicPropertySource
    @JvmStatic
    fun setUpProperties(registry: DynamicPropertyRegistry) {
      registry.add("spring.datasource.url") { postgresContainer.jdbcUrl }
      registry.add("spring.datasource.username") { postgresContainer.username }
      registry.add("spring.datasource.password") { postgresContainer.password }
    }
  }

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

    println("WIREMOCK PORT=$wiremockPort")

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

  fun getAllCourses(): List<Course> = performRequestAndExpectOk(
    HttpMethod.GET,
    "/courses",
    object : ParameterizedTypeReference<List<Course>>() {},
  )

  fun getAllOfferingsForCourse(courseId: UUID): List<CourseOffering> = performRequestAndExpectOk(
    HttpMethod.GET,
    "/courses/$courseId/offerings",
    object : ParameterizedTypeReference<List<CourseOffering>>() {},
  )

  fun <T> performRequestAndExpectOk(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
  ): T = performRequestAndExpectStatus(httpMethod, uri, returnType, HttpStatus.OK.value())

  fun <T> performRequestAndExpectStatus(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
    expectedResponseStatus: Int,
  ): T = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isEqualTo(expectedResponseStatus)
    .expectBody(returnType)
    .returnResult().responseBody!!

  fun <T> performRequestAndExpectStatusWithBody(
    httpMethod: HttpMethod,
    uri: String,
    returnType: ParameterizedTypeReference<T>,
    body: Any,
    expectedResponseStatus: Int,
  ): T = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(body)
    .exchange()
    .expectStatus().isEqualTo(expectedResponseStatus)
    .expectBody(returnType)
    .returnResult().responseBody!!

  fun performRequestAndExpectStatusWithBody(
    httpMethod: HttpMethod,
    uri: String,
    body: Any,
    expectedResponseStatus: Int,
  ): WebTestClient.ResponseSpec? = webTestClient
    .method(httpMethod)
    .uri(uri)
    .contentType(MediaType.APPLICATION_JSON)
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(body)
    .exchange()
    .expectStatus().isEqualTo(expectedResponseStatus)

  fun submitReferral(createdReferralId: UUID) = performRequestAndExpectOk(HttpMethod.POST, "/referrals/$createdReferralId/submit", referralTypeReference())
  fun updateReferral(referralId: UUID, referralUpdate: ReferralUpdate) = performRequestAndExpectStatusWithBody(HttpMethod.PUT, "/referrals/$referralId", referralUpdate, 204)

  fun referralTypeReference(): ParameterizedTypeReference<Referral> = object : ParameterizedTypeReference<Referral>() {}
  fun referralsListTypeReference(): ParameterizedTypeReference<List<Referral>> = object : ParameterizedTypeReference<List<Referral>>() {}
}
