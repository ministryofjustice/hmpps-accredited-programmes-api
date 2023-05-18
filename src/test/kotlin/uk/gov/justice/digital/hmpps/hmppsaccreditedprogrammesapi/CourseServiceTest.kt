package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseService
import java.util.*

class CourseServiceTest {
  private val service = CourseService()

  @Test
  fun `all courses`() {
    service.allCourses() shouldHaveSize 3
  }

  @Test
  fun `offerings for unknown course should be empty`() {
    service.offeringsForCourse(UUID.randomUUID()) should beEmpty()
  }

  @Test
  fun `offerings for a known course should return the offerings`() {
    val course = service.allCourses().find { it.name == "Thinking Skills Programme" }
    course.shouldNotBeNull()
    val offerings = service.offeringsForCourse(course.id)

    offerings.forAll { it.course.id shouldBe course.id }
    offerings shouldHaveSize 3
  }
}
