package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util

import io.kotest.matchers.string.shouldMatch
import org.junit.jupiter.api.RepeatedTest

private const val TEST_REPEATS = 20

class RandomDataGeneratorsTest {
  @RepeatedTest(TEST_REPEATS)
  fun `randomPrisonNumber should create random prison numbers`() {
    randomPrisonNumber() shouldMatch "^[A-Z][0-9]{4}[A-Z]{2}$"
  }

  @RepeatedTest(TEST_REPEATS)
  fun `capitalisedWord should create capitalised word`() {
    capitalisedWord(1..3).asString() shouldMatch "^[A-Z][a-z]{0,2}$"
  }

  @RepeatedTest(TEST_REPEATS)
  fun `randomSentence should create random sentences`() {
    randomSentence(1..3, 1..3) shouldMatch "^([A-Z][a-z]{0,2})( [a-z]{1,3}){0,2}$"
  }

  @RepeatedTest(TEST_REPEATS)
  fun `randomEmailAddress should create random email addresses`() {
    randomEmailAddress() shouldMatch "^[a-z]{5}\\.[a-z]{8}@[a-z]{6}\\.com$"
  }
}
