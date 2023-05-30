package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.inmemoryrepo

import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class InMemoryCourseRepositoryTest {
  private val repository = InMemoryCourseRepository()

  @Test
  fun `all courses`() {
    repository.allCourses() shouldHaveSize 3
  }

  @Test
  fun `a course`() {
    val aCourse = repository.allCourses().first()
    (repository.course(aCourse.id!!))
      .shouldNotBeNull()
      .shouldBeEqualToComparingFields(aCourse)
  }

  @Test
  fun `offerings for unknown course should be empty`() {
    repository.offeringsForCourse(UUID.randomUUID()) should beEmpty()
  }

  @Test
  fun `offerings for a known course should return the offerings`() {
    val course = repository.allCourses().find { it.name == "Thinking Skills Programme" }
    course.shouldNotBeNull()
    val offerings = repository.offeringsForCourse(course.id!!)

    offerings.forAll { it.course.id shouldBe course.id }
    offerings shouldHaveSize 3
  }

  @Test
  fun `find an offering by known course id and offering id - success`() {
    val courseId = repository.allCourses().find { it.name == "Thinking Skills Programme" }?.id
    courseId.shouldNotBeNull()

    val expectedOffering = repository.offeringsForCourse(courseId).find { it.organisationId == "BXI" }
    expectedOffering.shouldNotBeNull()

    val actualOffering = repository.courseOffering(courseId = courseId, offeringId = expectedOffering.id)
    actualOffering
      .shouldNotBeNull()
      .id shouldBe expectedOffering.id
  }

  @Test
  fun `find an offering by known course id and unknown offering id - fail`() {
    val courseId = repository.allCourses().find { it.name == "Thinking Skills Programme" }?.id
    courseId.shouldNotBeNull()

    repository.courseOffering(courseId = courseId, offeringId = UUID.randomUUID()).shouldBeNull()
  }

  @Test
  fun `find an offering by unknown course id and unknown offering id - fail`() {
    repository.courseOffering(courseId = UUID.randomUUID(), offeringId = UUID.randomUUID()).shouldBeNull()
  }
}
