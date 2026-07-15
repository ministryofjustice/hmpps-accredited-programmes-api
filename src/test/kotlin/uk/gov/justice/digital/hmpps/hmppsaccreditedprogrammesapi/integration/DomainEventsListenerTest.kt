package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.OffenderAllocationResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.PomDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PniResultRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.PersonIdentifier
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.PersonReference
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.SQSMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil.PrisonerFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PniResultEntityFactory
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.math.BigInteger
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Order(1)
class DomainEventsListenerTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralViewRepository: ReferralViewRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @Autowired
  lateinit var personRepository: PersonRepository

  @Autowired
  lateinit var courseParticipationRepository: CourseParticipationRepository

  @Autowired
  lateinit var pniResultRepository: PniResultRepository

  lateinit var offeringEntity: OfferingEntity

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    val courseEntity1 = CourseEntityFactory()
      .withIdentifier("SC")
      .withName("Super Course")
      .withDescription("Sample description")
      .withAlternateName("SC++")
      .withAudience("General offence")
      .produce()
    persistenceHelper.createCourse(courseEntity1)

    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")

    offeringEntity = OfferingEntityFactory()
      .withCourse(courseEntity1)
      .withOrganisationId("MDI")
      .produce()

    persistenceHelper.createOffering(offeringEntity)
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
  fun `should handle update offender message successfully`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val nomsNumber = "C6666DD"
    val offeringId = offeringEntity.id
    createReferral(offeringId!!, nomsNumber)

    val referralViewBeforeUpdate = referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    referralViewBeforeUpdate shouldNotBe null
    referralViewBeforeUpdate?.forename?.shouldBeEqual("JOHN")
    referralViewBeforeUpdate?.surname?.shouldBeEqual("SMITH")

    val prisoner = PrisonerFactory().withLastName("changed").withFirstName("name").produce()
    wiremockServer.stubFor(
      post(urlEqualTo("/prisoner-search/prisoner-numbers"))
        .withRequestBody(containing(nomsNumber))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(listOf(prisoner))),
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

    val referralViewAfterUpdate = referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }

    referralViewAfterUpdate shouldNotBe null
    referralViewAfterUpdate?.forename?.shouldBeEqual("name")
    referralViewAfterUpdate?.surname?.shouldBeEqual("changed")
  }

  @Test
  fun `should handle POM allocation message successfully`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val nomsNumber = "C6666CC"
    createReferral(offeringEntity.id!!, nomsNumber)

    val referralViewBeforeEvent = referralViewRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    referralViewBeforeEvent?.primaryPomUsername shouldBe null

    val offenderAllocationResponse = OffenderAllocationResponse(
      primaryPom = PomDetail(
        staffId = BigInteger.valueOf(487577),
        name = "Dave, Jones",
      ),
      secondaryPom = null,
    )

    wiremockServer.stubFor(
      get(urlEqualTo("/api/allocation/$nomsNumber"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(offenderAllocationResponse)),
        ),
    )

    val eventType = "offender-management.allocation.changed"
    sendDomainEvent(
      DomainEventsMessage(
        eventType,
        personReference = PersonReference(
          identifiers = listOf(
            PersonIdentifier("NOMS", nomsNumber),
          ),
        ),
      ),
    )

    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    await untilCallTo {
      referralRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    } matches { it?.primaryPomStaffId == "487577".toBigInteger() }
  }

  @Test
  fun `should handle prisoner merged message successfully and update prisoner number`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val nomsNumber = "A1234AA"
    val removedNomsNumber = "C6666DD"
    val createdReferral = createReferral(offeringEntity.id!!, removedNomsNumber)

    val referralViewBeforeUpdate = referralViewRepository.findAll().firstOrNull { it.prisonNumber == removedNomsNumber }
    referralViewBeforeUpdate shouldNotBe null
    referralViewBeforeUpdate?.prisonNumber?.shouldBe("C6666DD")

    persistenceHelper.createCourseParticipation(
      CourseParticipationEntityFactory()
        .withReferralId(createdReferral.id)
        .withPrisonNumber(removedNomsNumber)
        .produce(),
    )

    persistenceHelper.createPniResult(
      PniResultEntityFactory()
        .withPrisonNumber(removedNomsNumber)
        .withReferralId(createdReferral.id)
        .produce(),
    )

    // When
    val eventType = "prison-offender-events.prisoner.merged"
    sendDomainEvent(
      DomainEventsMessage(
        eventType,
        additionalInformation = mapOf(
          "nomsNumber" to nomsNumber,
          "removedNomsNumber" to removedNomsNumber,
        ),
      ),
    )

    // Then
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }

    // verify that the referral record has been updated with the new prisoner number
    val result = referralRepository.findAllByPrisonNumber(removedNomsNumber)
    assertThat(result).isEmpty()

    val postUpdateReferrals = referralRepository.findAllByPrisonNumber(nomsNumber)
    assertThat(postUpdateReferrals).hasSize(1)

    // verify that the person record has been updated with the new prisoner number
    personRepository.findPersonEntityByPrisonNumber(removedNomsNumber).shouldBeNull()
    val updatedPerson = personRepository.findPersonEntityByPrisonNumber(nomsNumber)
    assertThat(updatedPerson?.prisonNumber).isEqualTo(nomsNumber)

    // verify that the course participation record has been updated with the new prisoner number
    courseParticipationRepository.findByPrisonNumber(removedNomsNumber).shouldBeEmpty()
    val updatedCourseParticipation = courseParticipationRepository.findByPrisonNumber(nomsNumber).firstOrNull()
    assertThat(updatedCourseParticipation?.prisonNumber).isEqualTo(nomsNumber)

    // verify that the PNI result record has been updated with the new prisoner number
    pniResultRepository.findAllByPrisonNumber(removedNomsNumber).shouldBeEmpty()
    val updatedPniResult = pniResultRepository.findAllByPrisonNumber(nomsNumber).firstOrNull()
    assertThat(updatedPniResult?.prisonNumber).isEqualTo(nomsNumber)
  }

  fun createReferral(offeringId: UUID, prisonNumber: String = PRISON_NUMBER_1) = webTestClient
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
    .expectBody<Referral>()
    .returnResult().responseBody!!
}
