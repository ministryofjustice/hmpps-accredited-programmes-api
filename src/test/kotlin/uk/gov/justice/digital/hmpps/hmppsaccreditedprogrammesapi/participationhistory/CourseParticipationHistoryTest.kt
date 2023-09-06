package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shareddomain.BusinessException
import java.util.UUID

class CourseParticipationHistoryTest {
  @Test
  fun `validate - ok with courseId`() {
    shouldNotThrowAny { testObject(UUID.randomUUID(), null).assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `validate - ok with otherCourseName`() {
    shouldNotThrowAny { testObject(null, "Course Name").assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `validate - neither courseId or otherCourseName`() {
    shouldThrow<BusinessException> { testObject(null, null).assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `validate - both courseId and otherCourseName`() {
    shouldThrow<BusinessException> { testObject(UUID.randomUUID(), "Course Name").assertOnlyCourseIdOrCourseNamePresent() }
  }

  companion object {
    private fun testObject(courseId: UUID?, otherCourseName: String?) =
      CourseParticipationHistory(
        courseId = courseId,
        otherCourseName = otherCourseName,
        source = null,
        outcome = null,
        setting = null,
        prisonNumber = "A1234BC",
        yearStarted = null,
      )
  }
}
