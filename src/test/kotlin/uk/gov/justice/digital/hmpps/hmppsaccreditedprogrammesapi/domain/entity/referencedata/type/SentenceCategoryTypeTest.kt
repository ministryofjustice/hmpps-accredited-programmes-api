package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SentenceCategoryTypeTest {

  @Test
  fun `Should return RECALL if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.RECALL)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.RECALL)
  }

  @Test
  fun `Should return DETERMINATE if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE)
  }

  @Test
  fun `Should return INDETERMINATE if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.INDETERMINATE)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.INDETERMINATE)
  }

  @Test
  fun `Should return UNKNOWN if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.UNKNOWN)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.UNKNOWN)
  }

  @Test
  fun `Should return DETERMINATE_INDETERMINATE if list contains both DETERMINATE and INDETERMINATE and others are not present`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE, SentenceCategoryType.INDETERMINATE)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE_INDETERMINATE)
  }

  @Test
  fun `Should return DETERMINATE_INDETERMINATE_RECALL if list contains DETERMINATE_RECALL and INDETERMINATE and others are not present`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE_RECALL, SentenceCategoryType.INDETERMINATE)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE_INDETERMINATE_RECALL)
  }

  @Test
  fun `Should return DETERMINATE_INDETERMINATE_RECALL if list contains DETERMINATE and INDETERMINATE_RECALL and others are not present`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE, SentenceCategoryType.INDETERMINATE_RECALL)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE_INDETERMINATE_RECALL)
  }

  @Test
  fun `Should return UNKNOWN if all sentence categories are UNKNOWN`() {
    // Given
    val list = listOf(SentenceCategoryType.UNKNOWN, SentenceCategoryType.UNKNOWN)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.UNKNOWN)
  }

  @Test
  fun `Should return DETERMINATE if all other sentence categories are UNKNOWN`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE, SentenceCategoryType.UNKNOWN, SentenceCategoryType.UNKNOWN)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE)
  }

  @Test
  fun `Should return INDETERMINATE if all other sentence categories are UNKNOWN or RECALL`() {
    // Given
    val list = listOf(SentenceCategoryType.INDETERMINATE, SentenceCategoryType.UNKNOWN, SentenceCategoryType.RECALL)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.INDETERMINATE)
  }

  @Test
  fun `Should return DETERMINATE_RECALL if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.DETERMINATE_RECALL)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.DETERMINATE_RECALL)
  }

  @Test
  fun `Should return INDETERMINATE_RECALL if it's the only category in the list`() {
    // Given
    val list = listOf(SentenceCategoryType.INDETERMINATE_RECALL)
    // When & Then
    assertThat(SentenceCategoryType.determineOverallCategory(list)).isEqualTo(SentenceCategoryType.INDETERMINATE_RECALL)
  }
}
