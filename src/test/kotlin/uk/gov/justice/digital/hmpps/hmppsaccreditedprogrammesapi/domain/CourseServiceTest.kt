package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import java.util.*

class CourseServiceTest {
  private val repository = mockk<MutableCourseRepository>(relaxed = true)
  private val service = CourseService(repository)

  @Test
  fun `replaceAllCourses processes no courses`() {
    service.replaceAllCourses(emptyList())
    verify { repository.clear() }
    verify { repository.saveAudiences(emptySet()) }
  }

  @Test
  fun `replaceAllCourses processes one row`() {
    val a1 = Audience("Audience 1", id = UUID.randomUUID())

    every { repository.allAudiences() } returns setOf(a1)

    service.replaceAllCourses(
      listOf(
        CourseRecord(name = "Course", description = "Description", audience = "Audience 1", acronym = "CCC", comments = "A comment"),
      ),
    )

    verify { repository.clear() }
    verify { repository.saveAudiences(setOf(Audience(a1.value))) }
    verify {
      repository.saveCourse(eqCourse(CourseEntity(name = "Course", description = "Description", audiences = mutableSetOf(a1))))
    }
  }

  @Test
  fun `replaceAllCourses processes two rows`() {
    val a1 = Audience("Audience 1", id = UUID.randomUUID())
    val a2 = Audience("Audience 2", id = UUID.randomUUID())
    val a3 = Audience("Audience 3", id = UUID.randomUUID())

    every { repository.allAudiences() } returns setOf(a1, a2, a3)

    service.replaceAllCourses(
      listOf(
        CourseRecord(name = "Course 1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", acronym = "111", comments = "A comment for 1"),
        CourseRecord(name = "Course 2", description = "Description 2", audience = "${a1.value}, ${a3.value}", acronym = "222", comments = "A comment for 2"),
      ),
    )

    verify { repository.clear() }
    verify { repository.saveAudiences(setOf(Audience(a1.value), Audience(a2.value), Audience(a3.value))) }
    verify { repository.saveCourse(eqCourse(CourseEntity(name = "Course 1", description = "Description 1", audiences = mutableSetOf(a1, a2)))) }
    verify { repository.saveCourse(eqCourse(CourseEntity(name = "Course 2", description = "Description 2", audiences = mutableSetOf(a1, a3)))) }
  }

  @Test
  fun `Duplicate audience values are eliminated`() {
    val a1 = Audience("Audience 1", id = UUID.randomUUID())
    val a2 = Audience("Audience 2", id = UUID.randomUUID())
    val a3 = Audience("Audience 3", id = UUID.randomUUID())

    every { repository.allAudiences() } returns setOf(a1, a2, a3)

    service.replaceAllCourses(
      listOf(
        CourseRecord(name = "Course 1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", acronym = "111", comments = "A comment for 1"),
        CourseRecord(name = "Course 2", description = "Description 2", audience = "${a1.value}, ${a3.value}", acronym = "222", comments = "A comment for 2"),
        CourseRecord(name = "Course 3", description = "Description 3", audience = a1.value, acronym = "333", comments = "A comment for 3"),
        CourseRecord(name = "Course 4", description = "Description 4", audience = a1.value, acronym = "444", comments = "A comment for 4"),
      ),
    )

    verify { repository.saveAudiences(setOf(Audience(a1.value), Audience(a2.value), Audience(a3.value))) }
  }

  @Test
  fun `replaceAllPrerequisites - No records, No courses`() {
    service.replaceAllPrerequisites(emptyList())
  }

  @Test
  fun `replaceAllPrerequisites - No records, one course that has prerequisites`() {
    val allCourses = listOf(
      CourseEntity(
        name = "Course 1",
        description = "Description 1",
        prerequisites = mutableSetOf(
          Prerequisite(name = "PR 1", description = " PR Desc 1 "),
        ),
      ),
    )
    every { repository.allCourses() } returns allCourses

    service.replaceAllPrerequisites(emptyList())

    allCourses.flatMap { it.prerequisites }.shouldBeEmpty()
  }

  @Test
  fun `replaceAllPrerequisites - One record matching one course that has prerequisites`() {
    val allCourses = listOf(
      CourseEntity(
        name = "Course 1",
        description = "Description 1",
        prerequisites = mutableSetOf(
          Prerequisite(name = "PR 1", description = " PR 1 Desc"),
        ),
      ),
    )
    every { repository.allCourses() } returns allCourses

    service.replaceAllPrerequisites(
      listOf(
        PrerequisiteRecord("PR 2", "Course 1", description = "PR 2 Desc", comments = "Lorem ipsum"),
      ),
    )

    allCourses[0].prerequisites shouldContainExactly listOf(Prerequisite("PR 2", description = "PR 2 Desc"))
  }
}
