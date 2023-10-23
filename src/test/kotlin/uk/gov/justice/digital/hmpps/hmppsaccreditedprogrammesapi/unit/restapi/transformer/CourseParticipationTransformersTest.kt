package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.transformer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import jakarta.validation.ValidationException
import org.junit.jupiter.api.Test
import java.time.Year
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toDomain
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseParticipationOutcome as ApiCourseParticipationOutcome

class CourseParticipationTransformersTest {

  @Test
  fun `toDomain should transform valid ApiCourseParticipationOutcome correctly`() {
    val outcome = ApiCourseParticipationOutcome(
      status = ApiCourseParticipationOutcome.Status.complete,
      yearStarted = 1995,
      yearCompleted = 2000,
    ).toDomain()

    outcome.status shouldBe CourseStatus.COMPLETE
    outcome.yearStarted shouldBe Year.of(1995)
    outcome.yearCompleted shouldBe Year.of(2000)
  }

  @Test
  fun `toDomain should throw ValidationException for invalid yearStarted`() {
    val apiOutcome = ApiCourseParticipationOutcome(
      status = ApiCourseParticipationOutcome.Status.complete,
      yearStarted = 1985,
      yearCompleted = 2000,
    )

    shouldThrow<ValidationException> {
      apiOutcome.toDomain()
    }.message shouldBe "yearStarted is not valid."
  }

  @Test
  fun `toDomain should throw ValidationException for invalid yearCompleted`() {
    val apiOutcome = ApiCourseParticipationOutcome(
      status = ApiCourseParticipationOutcome.Status.complete,
      yearStarted = 1995,
      yearCompleted = 1985,
    )

    shouldThrow<ValidationException> {
      apiOutcome.toDomain()
    }.message shouldBe "yearCompleted is not valid."
  }
}
