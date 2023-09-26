package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport

import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.RepeatedTest

class RandomDataGeneratorsTest {
  @RepeatedTest(10)
  fun `should create random prison numbers`() {
    prisonNumber() shouldMatch "^[A-Z][0-9]{4}[A-Z]{2}$"
  }
}
