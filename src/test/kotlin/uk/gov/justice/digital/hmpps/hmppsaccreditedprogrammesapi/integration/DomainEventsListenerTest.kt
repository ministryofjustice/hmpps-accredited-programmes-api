package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.containing
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.ResourceLoader
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.SQSMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreated
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Order(1)
class DomainEventsListenerTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralViewRepository: ReferralViewRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createOffering(
      UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createCourse(
      UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      "CC",
      "Custom Course",
      "Sample description",
      "CC",
      "General offence",
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
    )
  }

  fun sendDomainEvent(
    message: DomainEventsMessage,
    queueUrl: String = domainEventQueue.queueUrl,
  ): SendMessageResponse = domainEventQueueClient.sendMessage(
    SendMessageRequest.builder()
      .queueUrl(queueUrl)
      .messageBody(
        objectMapper.writeValueAsString(SQSMessage(objectMapper.writeValueAsString(message))),
      ).build(),
  ).get()

  @Test
  fun `update offender message`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val nomsNumber = "C6666DD"
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    createReferral(offering.id!!, nomsNumber)

    val referralViewBefore = referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    referralViewBefore shouldNotBe null
    referralViewBefore?.forename?.shouldBeEqual("JOHN")
    referralViewBefore?.surname?.shouldBeEqual("SMITH")

    val results = ResourceLoader.file<List<Prisoner>>("prison-search-results")
    val result = results[0]
    result.lastName = "changed"
    result.firstName = "name"
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers")).withRequestBody(containing(nomsNumber))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(listOf(result))),
        ),
    )

    val eventType = "prisoner-offender-search.prisoner.updated"
    sendDomainEvent(
      DomainEventsMessage(
        eventType,
        additionalInformation = mapOf("nomsNumber" to nomsNumber),
      ),
    )
    // wait until the message is processed
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilCallTo {
      referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    } matches { it?.surname == "changed" }

    val referralViewAfter = referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }

    referralViewAfter shouldNotBe null
    referralViewAfter?.forename?.shouldBeEqual("name")
    referralViewAfter?.surname?.shouldBeEqual("changed")
  }

  fun createReferral(offeringId: UUID, prisonNumber: String = PRISON_NUMBER_1) =
    webTestClient
      .post()
      .uri("/referrals")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralCreate(
          offeringId = offeringId,
          prisonNumber = prisonNumber,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<ReferralCreated>()
      .returnResult().responseBody!!
}
