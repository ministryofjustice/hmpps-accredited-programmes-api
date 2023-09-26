package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.domain.BusinessException
import java.util.UUID

class CourseParticipationHistoryTest {

  private val factory = CourseParticipationHistoryEntityFactory()

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should successfully validate with valid courseId`() {
    val courseParticipationHistory = factory
      .withCourseId(UUID.randomUUID())
      .withOtherCourseName(null)
      .produce()

    shouldNotThrowAny { courseParticipationHistory.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should successfully validate with valid otherCourseName`() {
    val courseParticipationHistory = factory
      .withOtherCourseName("Course Name")
      .withCourseId(null)
      .produce()

    shouldNotThrowAny { courseParticipationHistory.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should throw exception when attempting to validate with neither courseId nor otherCourseName`() {
    val courseParticipationHistory = factory
      .withCourseId(null)
      .withOtherCourseName(null)
      .produce()

    shouldThrow<BusinessException> { courseParticipationHistory.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should throw exception when attempting to validate with both courseId and otherCourseName`() {
    val courseParticipationHistory = factory
      .withCourseId(UUID.randomUUID())
      .withOtherCourseName("Course Name")
      .produce()

    shouldThrow<BusinessException> { courseParticipationHistory.assertOnlyCourseIdOrCourseNamePresent() }
  }
}
