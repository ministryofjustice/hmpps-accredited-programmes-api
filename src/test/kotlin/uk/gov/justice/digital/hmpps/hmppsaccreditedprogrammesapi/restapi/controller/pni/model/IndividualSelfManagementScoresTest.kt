package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import java.util.stream.Stream

class IndividualSelfManagementScoresTest {

  companion object {
    @JvmStatic
    fun scoresForTotalScore(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(IndividualSelfManagementScores(1, 1, 1, 1), 4),
        Arguments.of(IndividualSelfManagementScores(null, 1, 1, 1), 3),
        Arguments.of(IndividualSelfManagementScores(1, null, 1, 1), 3),
        Arguments.of(IndividualSelfManagementScores(1, 1, null, 1), 3),
        Arguments.of(IndividualSelfManagementScores(1, 1, 1, null), 3),
        Arguments.of(IndividualSelfManagementScores(null, null, null, null), 0),
        Arguments.of(IndividualSelfManagementScores(2, 2, 2, 2), 8),
      )
    }

    @JvmStatic
    fun scoresForOverallSelfManagementScore(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(IndividualSelfManagementScores(0, 0, 0, 0), 0),
        Arguments.of(IndividualSelfManagementScores(null, 1, 2, 3), 2),
        Arguments.of(IndividualSelfManagementScores(1, null, 3, 4), 2),
        Arguments.of(IndividualSelfManagementScores(1, 2, null, 4), 2),
        Arguments.of(IndividualSelfManagementScores(1, 2, 3, null), 2),
        Arguments.of(IndividualSelfManagementScores(1, 0, 0, 0), 0),
        Arguments.of(IndividualSelfManagementScores(1, 1, 0, 0), 1),
        Arguments.of(IndividualSelfManagementScores(1, 1, 1, 0), 1),
        Arguments.of(IndividualSelfManagementScores(1, 1, 1, 1), 1),
        Arguments.of(IndividualSelfManagementScores(2, 1, 1, 1), 2),
        Arguments.of(IndividualSelfManagementScores(2, 2, 2, 2), 2),
      )
    }
  }

  @ParameterizedTest
  @MethodSource("scoresForTotalScore")
  fun `totalScore returned as expected`(scores: IndividualSelfManagementScores, expected: Int) {
    assertEquals(expected, scores.totalScore())
  }

  @ParameterizedTest
  @MethodSource("scoresForOverallSelfManagementScore")
  fun `overallSelfManagementScore returned as expected`(scores: IndividualSelfManagementScores, expected: Int?) {
    assertEquals(expected, scores.overallSelfManagementScore())
  }
}
