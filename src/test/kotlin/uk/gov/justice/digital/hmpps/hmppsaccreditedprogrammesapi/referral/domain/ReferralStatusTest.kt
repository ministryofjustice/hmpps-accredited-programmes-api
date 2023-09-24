package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.ASSESSMENT_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral.Status.REFERRAL_SUBMITTED
import java.util.stream.Stream

class ReferralStatusTest {
  @ParameterizedTest
  @MethodSource
  fun `isValidTransition should validate referral statuses`(from: Status, to: Status, expected: Boolean) {
    from.isValidTransition(to) shouldBe expected
  }

  companion object {
    private val validTransitions = mapOf(
      REFERRAL_STARTED to setOf(REFERRAL_STARTED, REFERRAL_SUBMITTED),
      REFERRAL_SUBMITTED to setOf(REFERRAL_SUBMITTED, AWAITING_ASSESSMENT),
      AWAITING_ASSESSMENT to setOf(AWAITING_ASSESSMENT, ASSESSMENT_STARTED),
      ASSESSMENT_STARTED to setOf(ASSESSMENT_STARTED),
    )

    @JvmStatic
    fun `isValidTransition should validate referral statuses`(): Stream<Arguments> =
      Status.entries.flatMap { from ->
        Status.entries.map { to ->
          arguments(from, to, validTransitions[from]!!.contains(to))
        }
      }.stream()
  }
}
