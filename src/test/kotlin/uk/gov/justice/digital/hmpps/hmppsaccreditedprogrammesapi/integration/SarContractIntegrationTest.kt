package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarApiDataTest
import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarIntegrationTestHelper

// import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarFlywaySchemaTest
// import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarJpaEntitiesTest
// import uk.gov.justice.digital.hmpps.subjectaccessrequest.SarReportTest

/**
 * Contract tests for the hmpps-subject-access-request library integration.
 * Inherits test contracts from the SAR testing library to validate that the application
 * correctly implements the Subject Access Request functionality.
 */
// @SpringBootTest(classes = [AccreditedProgrammesApi::class])
// @ActiveProfiles("test")
// @Tag("integration")
class SarContractIntegrationTest :
  IntegrationTestBase(),
  SarApiDataTest {
  override fun getWebTestClientInstance(): WebTestClient {
    TODO("Not yet implemented")
  }

  override fun setupTestData() {
    TODO("Not yet implemented")
  }

  override fun getSarHelper(): SarIntegrationTestHelper {
    TODO("Not yet implemented")
  }
}
//  SarReportTest,
//  SarFlywaySchemaTest,
//  SarJpaEntitiesTest
