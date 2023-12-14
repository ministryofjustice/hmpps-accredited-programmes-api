package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.pact

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.VerificationReports
import au.com.dius.pact.provider.junitsupport.loader.PactBroker
import au.com.dius.pact.provider.spring.junit5.PactVerificationSpringProvider
import org.apache.hc.core5.http.HttpRequest
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
@PactBroker
@Provider("Accredited Programmes API")
@VerificationReports(value = ["markdown", "console"], reportDir = "build/pact")
class PactContractTest {
  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  @MockBean
  lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @MockBean
  lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @State("Server is healthy")
  fun `ensure server is healthy`() {}

  @State("A participation can be created")
  fun `ensure a participation can be created`() {}

  @State("Course d3abc217-75ee-46e9-a010-368f30282367 exists")
  fun `ensure course d3abc217-75ee-46e9-a010-368f30282367 exists`() {}

  @State("Course d3abc217-75ee-46e9-a010-368f30282367 has offerings 790a2dfe-7de5-4504-bb9c-83e6e53a6537 and 7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  fun `ensure course d3abc217-75ee-46e9-a010-368f30282367 has offerings 790a2dfe-7de5-4504-bb9c-83e6e53a6537 and 7fffcc6a-11f8-4713-be35-cf5ff1aee517`() {}

  @State("Courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others exist")
  fun `ensure courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others exist`() {}

  @State("In order, the names of all the courses are Super Course, Custom Course, and RAPID Course")
  fun `ensure in order, the names of all the courses are Super Course, Custom Course, and RAPID Course`() {}

  @State("Offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists for course d3abc217-75ee-46e9-a010-368f30282367")
  fun `ensure offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists for course d3abc217-75ee-46e9-a010-368f30282367`() {}

  @State("Offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists")
  fun `ensure offering 790a2dfe-7de5-4504-bb9c-83e6e53a6537 exists`() {}

  @State("Offering 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists")
  fun `ensure offering 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists`() {}

  @State("Participation 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 exists")
  fun `ensure participation 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 exists`() {}

  @State("Participation 882a5a16-bcb8-4d8b-9692-a3006dcecffb exists")
  fun `ensure participation 882a5a16-bcb8-4d8b-9692-a3006dcecffb exists`() {}

  @State("Participation cc8eb19e-050a-4aa9-92e0-c654e5cfe281 exists")
  fun `ensure participation cc8eb19e-050a-4aa9-92e0-c654e5cfe281 exists`() {}

  @State("Person A1234AA has participations 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 and eb357e5d-5416-43bf-a8d2-0dc8fd92162e and no others")
  fun `ensure person A1234AA has participations 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004 and eb357e5d-5416-43bf-a8d2-0dc8fd92162e and no others`() {}

  @State("Referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists with status REFERRAL_STARTED")
  fun `ensure referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists with status REFERRAL_STARTED`() {}

  @State("Referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists")
  fun `ensure referral 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa exists`() {}

  @State("Referral(s) exist for organisation BWN")
  fun `ensure referrals exist for organisation BWN`() {}

  @State("Organisation BWN has courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others")
  fun `ensure organisation BWN has courses d3abc217-75ee-46e9-a010-368f30282367, 28e47d30-30bf-4dab-a8eb-9fda3f6400e8, and 1811faa6-d568-4fc4-83ce-41118b90242e and no others`() {}

  @State("Super Course referral(s) exist for organisation BWM with status REFERRAL_SUBMITTED to offerings for courses with audience General offence")
  fun `ensure Super Course referral(s) exist for organisation BWM with status REFERRAL_SUBMITTED to offerings for courses with audience General offence`() {}

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider::class)
  fun template(context: PactVerificationContext, request: HttpRequest) {
    request.setHeader(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    context.verifyInteraction()
  }
}
