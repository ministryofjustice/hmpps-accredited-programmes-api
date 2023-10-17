package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import java.util.UUID

class CourseParticipationTest {

  private val factory = CourseParticipationEntityFactory()

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should successfully validate with valid courseId`() {
    val courseParticipation = factory
      .withCourseId(UUID.randomUUID())
      .withOtherCourseName(null)
      .produce()

    shouldNotThrowAny { courseParticipation.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should successfully validate with valid otherCourseName`() {
    val courseParticipation = factory
      .withOtherCourseName("Course Name")
      .withCourseId(null)
      .produce()

    shouldNotThrowAny { courseParticipation.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should throw exception when attempting to validate with neither courseId nor otherCourseName`() {
    val courseParticipation = factory
      .withCourseId(null)
      .withOtherCourseName(null)
      .produce()

    shouldThrow<BusinessException> { courseParticipation.assertOnlyCourseIdOrCourseNamePresent() }
  }

  @Test
  fun `assertOnlyCourseIdOrCourseNamePresent should throw exception when attempting to validate with both courseId and otherCourseName`() {
    val courseParticipation = factory
      .withCourseId(UUID.randomUUID())
      .withOtherCourseName("Course Name")
      .produce()

    shouldThrow<BusinessException> { courseParticipation.assertOnlyCourseIdOrCourseNamePresent() }
  }
}
