package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeInRange
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.random.nextInt

class LoremIpsumTest {
  @RepeatedTest(10)
  fun `random words`() {
    val randomWords = LoremIpsum.words()
    randomWords.split(" ").size shouldBeInRange (1..20)
  }

  @RepeatedTest(10)
  fun `some random words`() {
    val wordCount = Random.nextInt(0..5)
    val randomWords = LoremIpsum.words(wordCount..wordCount)

    when (randomWords.length) {
      0 -> wordCount shouldBeEqual 0
      else -> randomWords.split(" ").shouldHaveSize(wordCount)
    }
  }

  @Test
  fun `no words`() {
    val randomWords = LoremIpsum.words(0..0)
    randomWords shouldBeEqual ""
  }
}
