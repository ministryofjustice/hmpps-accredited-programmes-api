package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType.DETERMINATE_RECALL
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType.INDETERMINATE_RECALL
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType.UNKNOWN
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SentenceCategoryRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var sentenceCategoryRepository: SentenceCategoryRepository

  @Test
  fun `should return existing sentence category for known description`() {
    // Given & When
    val sentenceCategoryEntity = sentenceCategoryRepository.findByDescription("ORA Recalled from Curfew Conditions")

    // Then
    sentenceCategoryEntity.shouldNotBeNull()
    sentenceCategoryEntity.category.shouldBeEqual(DETERMINATE_RECALL)
  }

  @Test
  fun `should return null for unknown category description`() {
    // Given & When
    val sentenceCategoryEntity = sentenceCategoryRepository.findByDescription("not-a-valid-description")

    // Then
    sentenceCategoryEntity.shouldBeNull()
  }

  @Test
  fun `should return all known sentence categories for valid descriptions`() {
    // Given
    val sentenceDescriptions = listOf("FTR Schedule 15 Offender", "Inability to Monitor", "Recall from Discretionary Life")

    // When
    val sentenceCategories = sentenceCategoryRepository.findAllByDescriptionIn(sentenceDescriptions)

    // Then
    sentenceCategories.size.shouldBeEqual(sentenceDescriptions.size)
    sentenceCategories.map { it.description }.shouldContainExactlyInAnyOrder(sentenceDescriptions)
    sentenceCategories.map { it.category }.shouldContainExactlyInAnyOrder(listOf(DETERMINATE_RECALL, DETERMINATE_RECALL, INDETERMINATE_RECALL))
  }

  @Test
  fun `should return only matching sentence categories for descriptions`() {
    // Given
    val sentenceDescriptions = listOf("Notice of Supervision", "INVALID-1", "INVALID-2")

    // When
    val sentenceCategories = sentenceCategoryRepository.findAllByDescriptionIn(sentenceDescriptions)

    // Then
    sentenceCategories.size.shouldBeEqual(1)
    sentenceCategories[0].description.shouldBeEqual("Notice of Supervision")
    sentenceCategories[0].category.shouldBeEqual(UNKNOWN)
  }
}
