package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.contract

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.fixture.JwtAuthHelper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
@PactBroker
@Provider("Accredited Programmes API")
class PactContractTest {
  @Autowired
  lateinit var jwtAuthHelper: JwtAuthHelper

  @State("Server is healthy")
  fun healthy() {
  }

  @TestTemplate
  @ExtendWith(PactVerificationSpringProvider::class)
  fun template(context: PactVerificationContext, request: HttpRequest) {
    request.setHeader(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    context.verifyInteraction()
  }
}
