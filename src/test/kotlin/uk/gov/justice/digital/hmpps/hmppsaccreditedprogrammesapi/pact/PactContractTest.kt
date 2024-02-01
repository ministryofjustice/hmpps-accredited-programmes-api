package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.pact

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.VerificationReports
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import com.github.tomakehurst.wiremock.client.WireMock
import org.apache.hc.core5.http.HttpRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_2
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
@PactBroker
@Provider("Accredited Programmes API")
@VerificationReports(value = ["markdown", "console"], reportDir = "build/pact")
class PactContractTest : IntegrationTestBase() {
  @BeforeEach
  fun setup() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withStatus(200)
            .withBody(
              objectMapper.writeValueAsString(listOf(PRISONER_1, PRISONER_2)),
            ),
        ),
    )
    persistenceHelper.clearAllTableContent()
  }

  @State("A participation can be created")
  fun `ensure a participation can be created`() {}

  @State("Course d3abc217-75ee-46e9-a010-368f30282367 exists")
  fun `ensure course d3abc217-75ee-46e9-a010-368f30282367 exists`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)
  }

  @State("Course d3abc217-75ee-46e9-a010-368f30282367 has offerings 790a2dfe-7de5-4504-bb9c-83e6e53a6537 and 7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  fun `ensure course d3abc217-75ee-46e9-a010-368f30282367 has offerings 790a2dfe-7de5-4504-bb9c-83e6e53a6537 and 7fffcc6a-11f8-4713-be35-cf5ff1aee517`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)
  }

  @State("Courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others exist")
  fun `ensure courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others exist`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createCourse(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"), "CC", "Custom Course", "Sample description", "CC", "General offence")
    persistenceHelper.createCourse(UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"), "RC", "RAPID Course", "Sample description", "RC", "General offence")
  }

  @State("In order, the names of all the courses are Super Course, Custom Course, and RAPID Course")
  fun `ensure in order, the names of all the courses are Super Course, Custom Course, and RAPID Course`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createCourse(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"), "CC", "Custom Course", "Sample description", "CC", "General offence")
    persistenceHelper.createCourse(UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"), "RC", "RAPID Course", "Sample description", "RC", "General offence")
  }

  @State("Offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists for course d3abc217-75ee-46e9-a010-368f30282367")
  fun `ensure offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists for course d3abc217-75ee-46e9-a010-368f30282367`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)
  }

  @State("Offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists")
  fun `ensure offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
  }

  @State("Offering 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists")
  fun `ensure offering 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)
  }

  @State("Participation 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 exists")
  fun `ensure participation 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 exists`() {
    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
  }

  @State("Participation 882a5a16-bcb8-4d8b-9692-a3006dcecffb exists")
  fun `ensure participation 882a5a16-bcb8-4d8b-9692-a3006dcecffb exists`() {
    persistenceHelper.createParticipation(UUID.fromString("882a5a16-bcb8-4d8b-9692-a3006dcecffb"), "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
  }

  @State("Participation cc8eb19e-050a-4aa9-92e0-c654e5cfe281 exists")
  fun `ensure participation cc8eb19e-050a-4aa9-92e0-c654e5cfe281 exists`() {
    persistenceHelper.createParticipation(UUID.fromString("cc8eb19e-050a-4aa9-92e0-c654e5cfe281"), "C1234CC", "Orange Course", "squirrel", "This participation will be updated", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
  }

  @State("Person A1234AA has participations 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 and eb357e5d-5416-43bf-a8d2-0dc8fd92162e and no others")
  fun `ensure person A1234AA has participations 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 and eb357e5d-5416-43bf-a8d2-0dc8fd92162e and no others`() {
    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e"), "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
  }

  @State("Referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists with status REFERRAL_STARTED")
  fun `ensure referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists with status REFERRAL_STARTED`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
  }

  @State("Referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists")
  fun `ensure referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
  }

  @State("Referral(s) exist for the logged in user")
  fun `ensure referrals exist for the logged in user`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
    persistenceHelper.createReferral(UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "C3456CC", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-12T19:11:00"))
    persistenceHelper.createReferral(UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "D3456DD", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-13T19:11:00"))

    // TODO: implement this on the UI side.
  }

  @State("Referral(s) exist for logged in user with status REFERRAL_SUBMITTED")
  fun `ensure referrals exist for logged in user with status REFERRAL_SUBMITTED`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "C3456CC", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-12T19:11:00"))
    persistenceHelper.createReferral(UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "D3456DD", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-13T19:11:00"))

    // TODO: implement this on the UI side.
  }

  @State("Referral(s) exist for organisation BWN")
  fun `ensure referrals exist for organisation BWN`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
    persistenceHelper.createReferral(UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "C3456CC", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-12T19:11:00"))
    persistenceHelper.createReferral(UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "D3456DD", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-13T19:11:00"))

    // TODO: implement this on the UI side.
  }

  @State("Organisation BWN has courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others")
  fun `ensure organisation BWN has courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createCourse(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"), "CC", "Custom Course", "Sample description", "CC", "General offence")
    persistenceHelper.createCourse(UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"), "RC", "RAPID Course", "Sample description", "RC", "General offence")

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
    persistenceHelper.createReferral(UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "C3456CC", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-12T19:11:00"))
    persistenceHelper.createReferral(UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "D3456DD", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-13T19:11:00"))
  }

  @State("Super Course referral(s) exist for organisation BWM with status REFERRAL_SUBMITTED to offerings for courses with audience General offence")
  fun `ensure Super Course referral(s) exist for organisation BWM with status REFERRAL_SUBMITTED to offerings for courses with audience General offence`() {
    persistenceHelper.createCourse(UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "SC", "Super Course", "Sample description", "SC++", "General offence")
    persistenceHelper.createOffering(UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "BWN", "nobody-bwn@digital.justice.gov.uk", "nobody2-bwn@digital.justice.gov.uk", true)
    persistenceHelper.createOffering(UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"), "MDI", "nobody-mdi@digital.justice.gov.uk", "nobody2-mdi@digital.justice.gov.uk", true)

    persistenceHelper.createCourse(UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"), "CC", "Custom Course", "Sample description", "CC", "General offence")
    persistenceHelper.createCourse(UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"), "RC", "RAPID Course", "Sample description", "RC", "General offence")

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_2")

    persistenceHelper.createReferral(UUID.fromString("0c46ed09-170b-4c0f-aee8-a24eeaeeddaa"), UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"), "B2345BB", "TEST_REFERRER_USER_1", "This referral will be updated", false, false, "REFERRAL_STARTED", null)
    persistenceHelper.createReferral(UUID.fromString("fae2ed00-057e-4179-9e55-f6a4f4874cf0"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "C3456CC", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-12T19:11:00"))
    persistenceHelper.createReferral(UUID.fromString("153383a4-b250-46a8-9950-43eb358c2805"), UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"), "D3456DD", "TEST_REFERRER_USER_2", "more information", true, true, "REFERRAL_SUBMITTED", LocalDateTime.parse("2023-11-13T19:11:00"))
  }

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider::class)
  fun template(context: PactVerificationContext, request: HttpRequest) {
    request.setHeader(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    context.verifyInteraction()
  }
}
