package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil.SentenceInformationFactory
import java.time.LocalDate

class PersonServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personService: PersonService

  private val testPrisonNumber = "A1234BC"

  @Test
  fun `determineSentenceType should return DETERMINATE when all sentences are determinate`() {
    // Given - using real sentence types from V94 migration
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("CJA03 Standard Determinate Sentence", "Adult Imprison above 12 mths below 4 yrs")
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Determinate"
  }

  @Test
  fun `determineSentenceType should return INDETERMINATE when all sentences are indeterminate`() {
    // Given - using real sentence types from V94 migration
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("Adult Discretionary Life", "Indeterminate Sentence for the Public Protection")
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Indeterminate"
  }

  @Test
  fun `determineSentenceType should return DETERMINATE_AND_INDETERMINATE when sentences are mixed`() {
    // Given - using real sentence types from V94 migration
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("CJA03 Standard Determinate Sentence", "Adult Discretionary Life")
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Determinate and Indeterminate"
  }

  @Test
  fun `determineSentenceType should return UNKNOWN when sentence types are not in database`() {
    // Given - using sentence types that don't exist in V94 migration data
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("Non-Existent Sentence Type 1", "Non-Existent Sentence Type 2")
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Unknown"
  }

  @Test
  fun `determineSentenceType should return NO_ACTIVE_SENTENCES when sentence information is null`() {
    // When
    val result = personService.determineSentenceType(null)

    // Then
    result shouldBe "No active sentences"
  }

  @Test
  fun `determineSentenceType should return NO_ACTIVE_SENTENCES when all sentences have end dates in the past`() {
    // Given - multiple sentences with different end dates, all in the past
    val factory = SentenceInformationFactory()

    val sentence1 = factory.createSentence(
      sentenceTypeDescription = "Sentence type 1",
      sentenceStartDate = LocalDate.of(2022, 1, 15),
      sentenceEndDate = LocalDate.now().minusDays(1),
      sentenceStatus = "ACTIVE",
    )
    val sentence2 = factory.createSentence(
      sentenceTypeDescription = "Sentence type 2",
      sentenceStartDate = LocalDate.of(2022, 3, 10),
      sentenceEndDate = LocalDate.now().minusMonths(3),
      sentenceStatus = "ACTIVE",
    )
    val sentence3 = factory.createSentence(
      sentenceTypeDescription = "Sentence type 3",
      sentenceStartDate = LocalDate.of(2021, 12, 5),
      sentenceEndDate = LocalDate.now().minusYears(1),
      sentenceStatus = "ACTIVE",
    )

    val courtSentence = factory.createCourtSentence(sentences = listOf(sentence1, sentence2, sentence3))
    val prisonTerm = factory.createPrisonTerm(courtSentences = listOf(courtSentence))
    val sentenceInformation = factory.createSentenceInformation(
      prisonerNumber = testPrisonNumber,
      latestPrisonTerm = prisonTerm,
    )

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "No active sentences"
  }

  @Test
  fun `determineSentenceType should return DETERMINATE when sentences have end dates in the future`() {
    // Given - sentences with end dates in the future
    val futureEndDate = LocalDate.now().plusYears(2)
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("CJA03 Standard Determinate Sentence", "Adult Imprison above 12 mths below 4 yrs")
      .withSentenceEndDate(futureEndDate)
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Determinate"
  }

  @Test
  fun `determineSentenceType should return INDETERMINATE when sentences have null end dates`() {
    // Given - sentences with null end dates should be considered active
    val sentenceInformation = SentenceInformationFactory()
      .withPrisonerNumber(testPrisonNumber)
      .withSentenceTypes("Adult Discretionary Life", "Indeterminate Sentence for the Public Protection")
      .withSentenceEndDate(null)
      .produce()

    // When
    val result = personService.determineSentenceType(sentenceInformation)

    // Then
    result shouldBe "Indeterminate"
  }
}
