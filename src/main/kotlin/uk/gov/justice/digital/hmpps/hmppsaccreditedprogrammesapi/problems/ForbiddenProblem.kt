package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.problems

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

class ForbiddenProblem() : AbstractThrowableProblem(null, "Forbidden", Status.FORBIDDEN, "You are not authorized to access this endpoint") {
  override fun getCause(): Exceptional? {
    return null
  }
}
