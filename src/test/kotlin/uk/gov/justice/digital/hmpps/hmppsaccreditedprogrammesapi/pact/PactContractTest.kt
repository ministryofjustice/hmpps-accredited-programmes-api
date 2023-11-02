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
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
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

  @State("Server is healthy")
  fun `ensure server is healthy`() {}

  @State("Participations exist for a person with prison number B1234BB")
  fun `ensure participations exist for a person with prison number B1234BB`() {}

  @State("A person exists with prison number A1234AA")
  fun `ensure a person exists with prison number A1234AA`() {}

  @State("Courses exist on the API")
  fun `ensure courses exist on the API`() {}

  @State("Course names exist on the API")
  fun `ensure course names exist on the API`() {}

  @State("A course exists with ID d3abc217-75ee-46e9-a010-368f30282367")
  fun `ensure a course exists with ID d3abc217-75ee-46e9-a010-368f30282367`() {}

  @State("A course participation exists with ID 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004")
  fun `ensure course participation exists with ID 0cff5da9-1e90-4ee2-a5cb-94dc49c4b004`() {}

  @State("A course participation exists with ID 1c0fbebe-7768-4dbe-ae58-6036183dbeff")
  fun `ensure course participation exists with ID 1c0fbebe-7768-4dbe-ae58-6036183dbeff`() {}

  @State("A referral exists with ID 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa")
  fun `ensure referral exists with ID 0c46ed09-170b-4c0f-aee8-a24eeaeeddaa`() {}

  @State("An offering exists with ID 7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  fun `ensure an offering exists with ID 7fffcc6a-11f8-4713-be35-cf5ff1aee517`() {}

  @State("An offering with ID 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists and has an associated course")
  fun `ensure An offering with ID 7fffcc6a-11f8-4713-be35-cf5ff1aee517 exists and has an associated course`() {}

  @State("Offerings exist for a course with ID d3abc217-75ee-46e9-a010-368f30282367")
  fun `ensure offerings exist for a course with ID d3abc217-75ee-46e9-a010-368f30282367`() {}

  @State("Referral can be created")
  fun `ensure referral can be created`() {}

  @State("Referral can be updated")
  fun `ensure referral can be updated`() {}

  @State("Participations exist for a person with prison number A1234AA")
  fun `ensure participations exist for a person with prison number A1234AA`() {}

  @State("Referral status can be updated")
  fun `ensure referral status can be updated`() {}

  @State("Referral can be submitted")
  fun `ensure referral can be submitted`() {}

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider::class)
  fun template(context: PactVerificationContext, request: HttpRequest) {
    request.setHeader(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    context.verifyInteraction()
  }
}
