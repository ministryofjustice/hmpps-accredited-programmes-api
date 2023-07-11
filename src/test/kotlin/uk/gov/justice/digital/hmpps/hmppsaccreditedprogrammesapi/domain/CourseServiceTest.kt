package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import java.util.UUID

class CourseServiceTest {
  private val repository = mockk<MutableCourseRepository>(relaxed = true)
  private val service = CourseService(repository)

  @Nested
  @DisplayName("Replace All Courses")
  inner class ReplaceAllCoursesTests {
    @Test
    fun `processes no courses`() {
      service.replaceAllCourses(emptyList())
      verify { repository.clear() }
      verify { repository.saveAudiences(emptySet()) }
    }

    @Test
    fun `processes one row`() {
      val a1 = Audience("Audience 1", id = UUID.randomUUID())

      every { repository.allAudiences() } returns setOf(a1)

      service.replaceAllCourses(
        listOf(
          NewCourse(name = "Course", identifier = "C", description = "Description", audience = "Audience 1", alternateName = "CCC"),
        ),
      )

      verify { repository.clear() }
      verify { repository.saveAudiences(setOf(Audience(a1.value))) }
      verify {
        repository.saveCourse(eqCourse(CourseEntity(name = "Course", identifier = "C", description = "Description", audiences = mutableSetOf(a1))))
      }
    }

    @Test
    fun `processes two rows`() {
      val a1 = Audience("Audience 1", id = UUID.randomUUID())
      val a2 = Audience("Audience 2", id = UUID.randomUUID())
      val a3 = Audience("Audience 3", id = UUID.randomUUID())

      every { repository.allAudiences() } returns setOf(a1, a2, a3)

      service.replaceAllCourses(
        listOf(
          NewCourse(name = "Course 1", identifier = "C1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", alternateName = "111"),
          NewCourse(name = "Course 2", identifier = "C2", description = "Description 2", audience = "${a1.value}, ${a3.value}", alternateName = "222"),
        ),
      )
      verify { repository.clear() }
      verify { repository.saveAudiences(setOf(Audience(a1.value), Audience(a2.value), Audience(a3.value))) }
      verify { repository.saveCourse(eqCourse(CourseEntity(name = "Course 1", identifier = "C1", description = "Description 1", audiences = mutableSetOf(a1, a2)))) }
      verify { repository.saveCourse(eqCourse(CourseEntity(name = "Course 2", identifier = "C2", description = "Description 2", audiences = mutableSetOf(a1, a3)))) }
    }

    @Test
    fun `Duplicate audience values are eliminated`() {
      val a1 = Audience("Audience 1", id = UUID.randomUUID())
      val a2 = Audience("Audience 2", id = UUID.randomUUID())
      val a3 = Audience("Audience 3", id = UUID.randomUUID())

      every { repository.allAudiences() } returns setOf(a1, a2, a3)

      service.replaceAllCourses(
        listOf(
          NewCourse(name = "Course 1", identifier = "C1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", alternateName = "111"),
          NewCourse(name = "Course 2", identifier = "C2", description = "Description 2", audience = "${a1.value}, ${a3.value}", alternateName = "222"),
          NewCourse(name = "Course 3", identifier = "C3", description = "Description 3", audience = a1.value, alternateName = "333"),
          NewCourse(name = "Course 4", identifier = "C4", description = "Description 4", audience = a1.value, alternateName = "444"),
        ),
      )

      verify { repository.saveAudiences(setOf(Audience(a1.value), Audience(a2.value), Audience(a3.value))) }
    }
  }

  @Nested
  @DisplayName("Replace All Prerequisites")
  inner class ReplaceAllPrerequisitesTests {
    @Test
    fun `No records, No courses`() {
      service.replaceAllPrerequisites(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `No records, one course that has prerequisites`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          prerequisites = mutableSetOf(
            Prerequisite(name = "PR 1", description = " PR Desc 1 "),
          ),
        ),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllPrerequisites(emptyList()).shouldBeEmpty()

      allCourses.flatMap { it.prerequisites }.shouldBeEmpty()
    }

    @Test
    fun `One record matching one course that has prerequisites`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          prerequisites = mutableSetOf(Prerequisite(name = "PR 1", description = " PR 1 Desc")),
        ),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllPrerequisites(
        listOf(
          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
        ),
      ).shouldBeEmpty()

      allCourses[0].prerequisites shouldContainExactly listOf(Prerequisite("PR 2", description = "PR 2 Desc"))
    }

    @Test
    fun `multiple courses and prerequisites - all match`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllPrerequisites(
        listOf(
          NewPrerequisite(name = "PR 1", description = "PR 1 Desc", identifier = "C1"),
          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
          NewPrerequisite(name = "PR 3", description = "PR 3 Desc", identifier = "C2"),
        ),
      ).shouldBeEmpty()

      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C1" to mutableSetOf(
          Prerequisite("PR 1", "PR 1 Desc"),
          Prerequisite("PR 2", "PR 2 Desc"),
        ),
        "C2" to mutableSetOf(
          Prerequisite("PR 3", "PR 3 Desc"),
        ),
      )
    }

    @Test
    fun `identifier mismatch - record ignored`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllPrerequisites(
        listOf(
          NewPrerequisite(name = "PR 1", description = "Don't care", identifier = "C1"),
          NewPrerequisite(name = "PR 1", description = "Don't care", identifier = "CX"),
          NewPrerequisite(name = "PR 2", description = "Don't care", identifier = "C2"),
        ),
      )
        .shouldContainExactly(
          LineMessage(
            lineNumber = 3,
            level = LineMessage.Level.error,
            message = "No match for course identifier 'CX'",
          ),
        )
    }

    @Test
    fun `NewPrerequisite has multiple identifiers`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C-1"),
        CourseEntity(name = "Course 2", identifier = "C-2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllPrerequisites(
        listOf(
          NewPrerequisite(name = "PR 1", description = "D1", identifier = " C-1 , C-2 "),
          NewPrerequisite(name = "PR 2", description = "D2", identifier = "C-2,C-X"),
        ),
      )

      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C-1" to mutableSetOf(Prerequisite("PR 1", "D1")),
        "C-2" to mutableSetOf(Prerequisite("PR 1", "D1"), Prerequisite("PR 2", "D2")),
      )
    }
  }

  @Nested
  @DisplayName("Replace All Offerings")
  inner class ReplaceAllOfferingsTests {
    @Test
    fun `No records, No courses`() {
      service.replaceAllOfferings(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `No records, one course that has offerings`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          offerings = mutableSetOf(
            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
          ),
        ),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllOfferings(emptyList()).shouldBeEmpty()

      allCourses.flatMap { it.offerings }.shouldBeEmpty()
    }

    @Test
    fun `One record matching one course that has offerings`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          offerings = mutableSetOf(
            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
          ),
        ),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllOfferings(
        listOf(
          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
        ),
      ).shouldBeEmpty()

      allCourses[0].offerings shouldContainExactly listOf(Offering(organisationId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"))
    }

    @Test
    fun `multiple courses and offerings - all match`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllOfferings(
        listOf(
          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "admin@mdi.net"),
          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
          NewOffering(identifier = "C2", prisonId = "MDI", contactEmail = "admin@mdi.net"),
        ),
      ).shouldBeEmpty()

      allCourses.associateBy(CourseEntity::name, CourseEntity::offerings) shouldBeEqual mapOf(
        "Course 1" to mutableSetOf(
          Offering(organisationId = "MDI", contactEmail = "admin@mdi.net"),
          Offering(organisationId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
        ),
        "Course 2" to mutableSetOf(
          Offering(organisationId = "MDI", contactEmail = "admin@mdi.net"),
        ),
      )
    }

    @Test
    fun `identifier mismatch - record ignored`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllOfferings(
        listOf(
          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net"),
          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
          NewOffering(identifier = "CX", prisonId = "BWI", contactEmail = "x@y.net"),
          NewOffering(identifier = "C2", prisonId = "MDI", contactEmail = "x@y.net"),
        ),
      )
        .shouldContainExactly(
          LineMessage(
            lineNumber = 4,
            level = LineMessage.Level.error,
            message = "No course matches offering with identifier 'CX' and prisonId 'BWI'",
          ),
        )
    }

    @Test
    fun `Missing contactEmail - Warning LineMessage produced`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { repository.allCourses() } returns allCourses

      service.replaceAllOfferings(
        listOf(
          NewOffering(identifier = "C1", prisonId = "MDI"),
          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
        ),
      )
        .shouldContainExactly(
          LineMessage(
            lineNumber = 2,
            level = LineMessage.Level.warning,
            message = "Missing contactEmail for offering with identifier 'C1' at prisonId 'MDI'",
          ),
        )
    }
  }
}
