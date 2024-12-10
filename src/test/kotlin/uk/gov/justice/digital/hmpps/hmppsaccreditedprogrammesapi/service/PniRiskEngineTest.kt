package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk
import java.math.BigDecimal

class PniRiskEngineTest {

  private val riskEngine = PniRiskEngine()

  @Test
  fun `isHighIntensityBasedOnRiskScores should return true for high OGRS3 and high OVP`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("75.00"),
      ovp = BigDecimal("60.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @Test
  fun `isHighIntensityBasedOnRiskScores should return true for high OGRS3 and high SARA`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = "High",
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @Test
  fun `isHighIntensityBasedOnRiskScores should return false if not high OGRS3`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("74.00"),
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
      ),
    )
    assertFalse(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @Test
  fun `isHighIntensityBasedOnRiskScores should return expected result if not high OVP or SARA`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = BigDecimal("59.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Medium",
        saraRiskOfViolenceTowardsOthers = "Medium",
      ),
    )
    assertFalse(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if ogrs3 is high`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if ovp is high`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if ospDc is high`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = "VERY_HIGH",
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should return isHighRisk for a male with High or Very High ospIic `(ospIicValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = ospIicValue,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighRisk(riskScores, "Male"))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should return isHighRisk for a male with High or Very High ospDc `(ospDcValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = ospDcValue,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighRisk(riskScores, "Male"))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should return isHighRisk for a person with High or Very High Sara risk of violence towards partner`(saraRiskValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = saraRiskValue,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighRisk(riskScores, "Male"))
    assertTrue(riskEngine.isHighRisk(riskScores, "Female"))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should return isHighRisk for a person with High or Very High Sara risk of violence towards others`(saraRiskValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = saraRiskValue,
        overallResult = null,
      ),
    )
    assertTrue(riskEngine.isHighRisk(riskScores, "Male"))
    assertTrue(riskEngine.isHighRisk(riskScores, "Female"))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should NOT return isHighRisk for a female with High or Very High ospDc`(ospDcValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = ospDcValue,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertFalse(riskEngine.isHighRisk(riskScores, "Female"))
  }

  @ParameterizedTest
  @CsvSource(value = ["VERY_HIGH", "VERY HIGH", "Very High", "HIGH", "High"])
  fun `should NOT return isHighRisk for a female with High or Very High ospIic`(ospIicValue: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = ospIicValue,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertFalse(riskEngine.isHighRisk(riskScores, "Female"))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if ospIic is high`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = "VERY_HIGH",
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if rsr is high and no osp scores`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = BigDecimal("3.00"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isHighRisk should return expected result if sara is high`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "High",
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,false"], delimiter = ',')
  fun `isHighRisk should return expected result if no high scores`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = "NOT_APPLICABLE",
      ospIic = "LOW",
      rsr = BigDecimal("1.00"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Low",
        overallResult = SaraRisk.LOW,
      ),
    )
    assertEquals(result, riskEngine.isHighRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if ogrs3 is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if ovp is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("30.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if ospDc is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = "MEDIUM",
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if ospIic is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = "MEDIUM",
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @Test
  fun `should NOT return a medium risk when rsr is high and no osp scores are present`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("60.00"),
      ospDc = null,
      ospIic = null,
      rsr = BigDecimal("3.41"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertThat(riskEngine.isMediumRisk(riskScores, "Male")).isFalse
  }

  @Test
  fun `should use RSR score to calculate high risk when OSP scores are set to Not Applicable`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = "Not Applicable",
      ospIic = "Not Applicable",
      rsr = BigDecimal("3.41"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertThat(riskEngine.isHighRisk(riskScores, "Male")).isTrue
  }

  @Test
  fun `should use RSR score to calculate medium risk when OSP scores are set to Not Applicable`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = "Not Applicable",
      ospIic = "Not Applicable",
      rsr = BigDecimal("2.99"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertThat(riskEngine.isMediumRisk(riskScores, "Male")).isTrue
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if rsr is medium and no osp scores`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = BigDecimal("2.50"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = null,
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if either sara score is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Medium",
        saraRiskOfViolenceTowardsOthers = null,
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,true", "Male,true"], delimiter = ',')
  fun `isMediumRisk should return expected result if sara is medium`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Medium",
        overallResult = null,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female,false", "Male,false"], delimiter = ',')
  fun `isMediumRisk should return expected result if no medium scores`(gender: String, result: Boolean) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("20.00"),
      ovp = BigDecimal("20.00"),
      ospDc = "HIGH",
      ospIic = "VERY_HIGH",
      rsr = BigDecimal("0.50"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Low",
        overallResult = SaraRisk.LOW,
      ),
    )
    assertEquals(result, riskEngine.isMediumRisk(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female", "Male"])
  fun `getRiskClassification should return HIGH_RISK if isHighRisk returns true`(gender: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = "VERY_HIGH",
      ospIic = "HIGH",
      rsr = BigDecimal("3.00"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Low",
        overallResult = SaraRisk.LOW,
      ),
    )
    assertEquals(RiskClassification.HIGH_RISK.name, riskEngine.getRiskClassification(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female", "Male"])
  fun `getRiskClassification should return MEDIUM_RISK if isMediumRisk returns true`(gender: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = "MEDIUM",
      ospIic = "MEDIUM",
      rsr = BigDecimal("2.50"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Low",
        overallResult = SaraRisk.LOW,
      ),
    )
    assertEquals(RiskClassification.MEDIUM_RISK.name, riskEngine.getRiskClassification(riskScores, gender))
  }

  @ParameterizedTest
  @CsvSource(value = ["Female", "Male"])
  fun `getRiskClassification should return LOW_RISK if no high or medium risk`(gender: String) {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("20.00"),
      ovp = BigDecimal("20.00"),
      ospDc = "LOW",
      ospIic = "LOW",
      rsr = BigDecimal("0.50"),
      sara = Sara(
        saraRiskOfViolenceTowardsPartner = "Low",
        saraRiskOfViolenceTowardsOthers = "Low",
        overallResult = SaraRisk.LOW,
      ),
    )
    assertEquals(RiskClassification.LOW_RISK.name, riskEngine.getRiskClassification(riskScores, gender))
  }
}
