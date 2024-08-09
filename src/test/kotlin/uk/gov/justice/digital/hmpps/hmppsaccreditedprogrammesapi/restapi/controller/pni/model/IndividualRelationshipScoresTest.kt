package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import java.util.stream.Stream

class IndividualRelationshipScoresTest {

  companion object {
    @JvmStatic
    fun scoresForTotalScore(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(IndividualRelationshipScores(1, 1, 1, 1), 4),
        Arguments.of(IndividualRelationshipScores(null, 1, 1, 1), 3),
        Arguments.of(IndividualRelationshipScores(1, null, 1, 1), 3),
        Arguments.of(IndividualRelationshipScores(1, 1, null, 1), 3),
        Arguments.of(IndividualRelationshipScores(1, 1, 1, null), 3),
        Arguments.of(IndividualRelationshipScores(null, null, null, null), 0),
        Arguments.of(IndividualRelationshipScores(2, 2, 2, 2), 8),
      )
    }

    @JvmStatic
    fun scoresForOverallRelationshipScore(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(IndividualRelationshipScores(0, 0, 0, 0), 0),
        Arguments.of(IndividualRelationshipScores(1, 0, 0, 0), 0),
        Arguments.of(IndividualRelationshipScores(1, 1, 0, 0), 1),
        Arguments.of(IndividualRelationshipScores(1, 1, 1, 0), 1),
        Arguments.of(IndividualRelationshipScores(1, 1, 1, 1), 1),
        Arguments.of(IndividualRelationshipScores(2, 1, 1, 1), 2),
        Arguments.of(IndividualRelationshipScores(2, 2, 2, 2), 2),
      )
    }
  }

  @ParameterizedTest
  @MethodSource("scoresForTotalScore")
  fun `totalScore returned as expected`(scores: IndividualRelationshipScores, expected: Int) {
    assertEquals(expected, scores.totalScore())
  }

  @ParameterizedTest
  @MethodSource("scoresForOverallRelationshipScore")
  fun `overall relationship score returned as expected`(scores: IndividualRelationshipScores, expected: Int) {
    assertEquals(expected, scores.overallRelationshipScore())
  }
}
