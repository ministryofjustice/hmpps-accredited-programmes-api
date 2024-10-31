package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.jupiter.api.Test

class SaraRiskTest {

  @Test
  fun `very high is the highest risk`() {
    // Given
    val risk1 = SaraRisk.LOW
    val risk2 = SaraRisk.VERY_HIGH

    // When
    val highestRisk = SaraRisk.highestRisk(risk1, risk2)

    // Then
    assertThat(highestRisk).isEqualTo(SaraRisk.VERY_HIGH)
  }

  @Test
  fun `medium is higher than low`() {
    // Given
    val risk1 = SaraRisk.LOW
    val risk2 = SaraRisk.MEDIUM

    // When
    val highestRisk = SaraRisk.highestRisk(risk1, risk2)

    // Then
    assertThat(highestRisk).isEqualTo(SaraRisk.MEDIUM)
  }

  @Test
  fun `fromString should return VERY_HIGH when value is VERY_HIGH`() {
    // Given & When
    val saraRisk = SaraRisk.fromString("VERY_HIGH")
    // Then
    assertThat(saraRisk).isSameAs(SaraRisk.VERY_HIGH)
  }

  @Test
  fun `fromString should return LOW when value is Low`() {
    // Given & When
    val saraRisk = SaraRisk.fromString("Low")
    // Then
    assertThat(saraRisk).isSameAs(SaraRisk.LOW)
  }

  @Test
  fun `fromString should NOT_APPLICABLE for null risk`() {
    // Given & When
    val saraRisk = SaraRisk.fromString(null)
    // Then
    assertThat(saraRisk).isSameAs(SaraRisk.NOT_APPLICABLE)
  }

  @Test
  fun `should return not applicable when sara risk does not exist`() {
    // Given
    val risk1 = SaraRisk.NOT_APPLICABLE
    val risk2 = SaraRisk.NOT_APPLICABLE

    // When
    val highestRisk = SaraRisk.highestRisk(risk1, risk2)

    // Then
    assertThat(highestRisk).isEqualTo(SaraRisk.NOT_APPLICABLE)
  }

  @Test
  fun `low is lower than high`() {
    // Given
    val risk1 = SaraRisk.LOW
    val risk2 = SaraRisk.HIGH

    // When
    val highestRisk = SaraRisk.highestRisk(risk1, risk2)

    // Then
    assertThat(highestRisk).isEqualTo(SaraRisk.HIGH)
  }

  @Test
  fun `not applicable is the lowest risk`() {
    val risk1 = SaraRisk.NOT_APPLICABLE
    val risk2 = SaraRisk.LOW

    val highestRisk = SaraRisk.highestRisk(risk1, risk2)

    assertThat(highestRisk).isEqualTo(SaraRisk.LOW)
  }
}
