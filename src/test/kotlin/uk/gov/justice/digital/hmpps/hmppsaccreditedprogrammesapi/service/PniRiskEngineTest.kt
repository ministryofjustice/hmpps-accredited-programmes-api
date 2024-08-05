package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model.IndividualRiskScores
import java.math.BigDecimal

class PniRiskEngineTest {

  private val riskEngine = PniRiskEngine()

  @Test
  fun `getOverallRiskScore should return HIGH_OGRS when ogrs3 is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    val riskScore = riskEngine.getOverallRiskScore(riskScores, "A12345")
    assertEquals(RiskClassification.HIGH_OGRS.name, riskScore.classification)
  }

  @Test
  fun `getOverallRiskScore should return HIGH_OVP when ovp is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    val riskScore = riskEngine.getOverallRiskScore(riskScores, "A12345")
    assertEquals(RiskClassification.HIGH_OVP.name, riskScore.classification)
  }

  @Test
  fun `getOverallRiskScore should return HIGH_SARA when sara is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "High",
    )
    val riskScore = riskEngine.getOverallRiskScore(riskScores, "A12345")
    assertEquals(RiskClassification.HIGH_SARA.name, riskScore.classification)
  }

  @Test
  fun `getOverallRiskScore should return MEDIUM_SARA when sara is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "Medium",
    )
    val riskScore = riskEngine.getOverallRiskScore(riskScores, "A12345")
    assertEquals(RiskClassification.MEDIUM_SARA.name, riskScore.classification)
  }

  @Test
  fun `isHighIntensityBasedOnRiskScores should return true for high OGRS3 and high OVP`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
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
      sara = "High",
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
      sara = null,
    )
    assertFalse(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @Test
  fun `isHighIntensityBasedOnRiskScores should return false if not high OVP or SARA`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = BigDecimal("59.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "Medium",
    )
    assertFalse(riskEngine.isHighIntensityBasedOnRiskScores(riskScores))
  }

  @Test
  fun `isHighRisk should return true if ogrs3 is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return true if ovp is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return true if ospDc is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = BigDecimal("35.00"),
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return true if ospIic is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = BigDecimal("35.00"),
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return true if rsr is high and no osp scores`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = BigDecimal("3.00"),
      sara = null,
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return true if sara is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "High",
    )
    assertTrue(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isHighRisk should return false if no high scores`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = BigDecimal("20.00"),
      ospIic = BigDecimal("20.00"),
      rsr = BigDecimal("1.00"),
      sara = "Low",
    )
    assertFalse(riskEngine.isHighRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if ogrs3 is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if ovp is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("30.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if ospDc is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = BigDecimal("30.00"),
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if ospIic is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = BigDecimal("30.00"),
      rsr = null,
      sara = null,
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if rsr is medium and no osp scores`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = BigDecimal("2.50"),
      sara = null,
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return true if sara is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "Medium",
    )
    assertTrue(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `isMediumRisk should return false if no medium scores`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("20.00"),
      ovp = BigDecimal("20.00"),
      ospDc = BigDecimal("20.00"),
      ospIic = BigDecimal("20.00"),
      rsr = BigDecimal("0.50"),
      sara = "Low",
    )
    assertFalse(riskEngine.isMediumRisk(riskScores))
  }

  @Test
  fun `getRiskClassification should return HIGH_OGRS if ogrs3 is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("76.00"),
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertEquals(RiskClassification.HIGH_OGRS.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return HIGH_OVP if ovp is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = BigDecimal("61.00"),
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = null,
    )
    assertEquals(RiskClassification.HIGH_OVP.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return HIGH_SARA if sara is high`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "High",
    )
    assertEquals(RiskClassification.HIGH_SARA.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return MEDIUM_SARA if sara is medium`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = null,
      ovp = null,
      ospDc = null,
      ospIic = null,
      rsr = null,
      sara = "Medium",
    )
    assertEquals(RiskClassification.MEDIUM_SARA.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return HIGH_RISK if isHighRisk returns true`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = BigDecimal("35.00"),
      ospIic = BigDecimal("35.00"),
      rsr = BigDecimal("3.00"),
      sara = "Low",
    )
    assertEquals(RiskClassification.HIGH_RISK.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return MEDIUM_RISK if isMediumRisk returns true`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("50.00"),
      ovp = BigDecimal("30.00"),
      ospDc = BigDecimal("30.00"),
      ospIic = BigDecimal("30.00"),
      rsr = BigDecimal("2.50"),
      sara = "Low",
    )
    assertEquals(RiskClassification.MEDIUM_RISK.name, riskEngine.getRiskClassification(riskScores))
  }

  @Test
  fun `getRiskClassification should return LOW_RISK if no high or medium risk`() {
    val riskScores = IndividualRiskScores(
      ogrs3 = BigDecimal("20.00"),
      ovp = BigDecimal("20.00"),
      ospDc = BigDecimal("20.00"),
      ospIic = BigDecimal("20.00"),
      rsr = BigDecimal("0.50"),
      sara = "Low",
    )
    assertEquals(RiskClassification.LOW_RISK.name, riskEngine.getRiskClassification(riskScores))
  }
}
