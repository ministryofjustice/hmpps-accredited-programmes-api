package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import java.util.stream.Stream

class IndividualSexScoresTest {
  companion object {
    @JvmStatic
    fun hasSomeDataPresent(): Stream<Arguments> = Stream.of(
      Arguments.of(IndividualSexScores(null, 1, 1), true),
      Arguments.of(IndividualSexScores(1, null, 1), true),
      Arguments.of(IndividualSexScores(1, 1, null), true),
      Arguments.of(IndividualSexScores(1, 1, 1), true),
      Arguments.of(IndividualSexScores(null, null, null), false),
    )

    @JvmStatic
    fun scoresForTotalScore(): Stream<Arguments> = Stream.of(
      Arguments.of(IndividualSexScores(1, 1, 1), 3),
      Arguments.of(IndividualSexScores(null, 1, 1), 2),
      Arguments.of(IndividualSexScores(1, null, 1), 2),
      Arguments.of(IndividualSexScores(1, 1, null), 2),
      Arguments.of(IndividualSexScores(null, null, null), 0),
    )

    @JvmStatic
    fun scoresForOverallSexDomainScore(): Stream<Arguments> = Stream.of(
      Arguments.of(IndividualSexScores(0, 0, 0), 0, 0),
      Arguments.of(IndividualSexScores(null, 1, 2), 1, 0),
      Arguments.of(IndividualSexScores(1, null, 3), 2, 1),
      Arguments.of(IndividualSexScores(1, 2, null), 2, 2),
      Arguments.of(IndividualSexScores(null, null, null), 0, 0),
      Arguments.of(IndividualSexScores(0, 1, 0), 1, 0),
      Arguments.of(IndividualSexScores(1, 1, 1), 3, 1),
      Arguments.of(IndividualSexScores(2, 2, 2), 6, 2),
      Arguments.of(IndividualSexScores(3, 2, 3), 6, 2),
      Arguments.of(IndividualSexScores(1, 2, 1), 4, 2),
      Arguments.of(IndividualSexScores(1, 2, 1), 2, 2),
    )
  }

  @ParameterizedTest
  @MethodSource("hasSomeDataPresent")
  fun `hasSomeDataPresent method`(scores: IndividualSexScores, expected: Boolean) {
    assertEquals(expected, scores.hasSomeDataPresent())
  }

  @ParameterizedTest
  @MethodSource("scoresForTotalScore")
  fun `test totalScore method`(scores: IndividualSexScores, expected: Int) {
    assertEquals(expected, scores.totalScore())
  }

  @ParameterizedTest
  @MethodSource("scoresForOverallSexDomainScore")
  fun `test overallSexDomainScore method`(scores: IndividualSexScores, totalScore: Int?, expected: Int?) {
    assertEquals(expected, scores.overallSexDomainScore(totalScore))
  }
}
