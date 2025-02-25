package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import java.util.stream.Stream

class IndividualCognitiveScoresTest {

  companion object {
    @JvmStatic
    fun scoresForTotalScore(): Stream<Arguments> = Stream.of(
      Arguments.of(IndividualCognitiveScores(1, 1), 2),
      Arguments.of(IndividualCognitiveScores(null, 1), 1),
      Arguments.of(IndividualCognitiveScores(1, null), 1),
      Arguments.of(IndividualCognitiveScores(null, null), 0),
      Arguments.of(IndividualCognitiveScores(2, 2), 4),
    )

    @JvmStatic
    fun scoresForOverallCognitiveDomainScore(): Stream<Arguments> = Stream.of(
      Arguments.of(IndividualCognitiveScores(0, 0), 0),
      Arguments.of(IndividualCognitiveScores(1, null), 1),
      Arguments.of(IndividualCognitiveScores(null, 2), 1),
      Arguments.of(IndividualCognitiveScores(1, 0), 1),
      Arguments.of(IndividualCognitiveScores(1, 1), 1),
      Arguments.of(IndividualCognitiveScores(2, 1), 2),
      Arguments.of(IndividualCognitiveScores(2, 2), 2),
      Arguments.of(IndividualCognitiveScores(2, 0), 2),
    )
  }

  @ParameterizedTest
  @MethodSource("scoresForTotalScore")
  fun `totalScore returned as expected`(scores: IndividualCognitiveScores, expected: Int) {
    assertEquals(expected, scores.totalScore())
  }

  @ParameterizedTest
  @MethodSource("scoresForOverallCognitiveDomainScore")
  fun `overallCognitiveDomainScore returned as expected`(scores: IndividualCognitiveScores, expected: Int?) {
    assertEquals(expected, scores.overallCognitiveDomainScore())
  }
}
