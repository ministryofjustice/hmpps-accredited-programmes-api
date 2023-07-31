package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

class FakeRepository : CourseRepository {
  private val courses: MutableList<CourseEntity> = mutableListOf()
  private val audiences: MutableSet<Audience> = mutableSetOf()
  override fun allCourses(): List<CourseEntity> = courses.toList()
  override fun allActiveCourses(): List<CourseEntity> = TODO("Not yet implemented")
  override fun course(courseId: UUID): CourseEntity? = TODO("Not yet implemented")
  override fun saveCourse(courseEntity: CourseEntity) {
    courses.add(courseEntity)
  }

  override fun offeringsForCourse(courseId: UUID): List<Offering> = TODO("Not yet implemented")
  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = TODO("Not yet implemented")
  override fun allAudiences(): Set<Audience> = audiences.toSet()
  override fun saveAudiences(audiences: Set<Audience>) {
    this.audiences.addAll(audiences)
  }
}

class CourseServiceTest {
  private val repository = FakeRepository()
  private val service = CourseService(repository)

  @Nested
  @DisplayName("Update All Courses")
  inner class UpdateAllCoursesTests {
    @Test
    fun `Empty repository no updates`() {
      service.updateCourses(emptyList())
      repository.allCourses().shouldBeEmpty()
      repository.allAudiences().shouldBeEmpty()
    }

    @Test
    fun `Add one course`() {
      service.updateCourses(listOf(NewCourse(name = "Course", identifier = "C", description = "Description", audience = "", alternateName = "CCC")))
      repository.allAudiences().shouldBeEmpty()
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(
          name = "Course",
          alternateName = "CCC",
          identifier = "C",
          description = "Description",
        ),
      )
    }

    @Test
    fun `Update one course`() {
      repository.saveCourse(CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description"))
      repository.allCourses() shouldHaveSize 1

      service.updateCourses(listOf(NewCourse(name = "Updated Course", identifier = "C", description = "Updated Description", audience = "", alternateName = "UCCC")))

      repository.allAudiences().shouldBeEmpty()
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(name = "Updated Course", alternateName = "UCCC", identifier = "C", description = "Updated Description"),
      )
    }

    @Test
    fun `Withdraw one course`() {
      repository.saveCourse(CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description"))
      repository.allCourses() shouldHaveSize 1

      service.updateCourses(listOf())

      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description", withdrawn = true),
      )
    }

    @Test
    fun `Add one course with audiences`() {
      service.updateCourses(listOf(NewCourse(name = "Course", identifier = "C", description = "Description", audience = "A, B , C, ", alternateName = "CCC")))
      repository.allAudiences() shouldHaveSize 3
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(
          name = "Course",
          alternateName = "CCC",
          identifier = "C",
          description = "Description",
        ).apply {
          audiences.addAll(setOf(Audience("A"), Audience("B"), Audience("C")))
        },
      )
    }

    @Test
    fun `Update course audiences`() {
      service.updateCourses(listOf(NewCourse(name = "Course", identifier = "C", description = "Description", audience = "A, B, C", alternateName = "CCC")))

      repository.allAudiences().shouldContainExactly(Audience("A"), Audience("B"), Audience("C"))

      service.updateCourses(listOf(NewCourse(name = "Course", identifier = "C", description = "Description", audience = "A, D, ", alternateName = "CCC")))
      repository.allAudiences().shouldContainExactly(Audience("A"), Audience("B"), Audience("C"), Audience("D"))
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description").apply {
          audiences.addAll(setOf(Audience("A"), Audience("D")))
        },
      )
    }
  }

  @Nested
  @DisplayName("Replace All Prerequisites")
  inner class ReplaceAllPrerequisitesTests {
    @Test
    fun `No records, No courses`() {
      service.replaceAllPrerequisites(emptyList()).shouldBeEmpty()
    }
//
//    @Test
//    fun `No records, one course that has prerequisites`() {
//      val allCourses = listOf(
//        CourseEntity(
//          name = "Course 1",
//          identifier = "C1",
//          description = "Description 1",
//          prerequisites = mutableSetOf(
//            Prerequisite(name = "PR 1", description = " PR Desc 1 "),
//          ),
//        ),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllPrerequisites(emptyList()).shouldBeEmpty()
//
//      allCourses.flatMap { it.prerequisites }.shouldBeEmpty()
//    }
//
//    @Test
//    fun `One record matching one course that has prerequisites`() {
//      val allCourses = listOf(
//        CourseEntity(
//          name = "Course 1",
//          identifier = "C1",
//          prerequisites = mutableSetOf(Prerequisite(name = "PR 1", description = " PR 1 Desc")),
//        ),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllPrerequisites(
//        listOf(
//          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
//        ),
//      ).shouldBeEmpty()
//
//      allCourses[0].prerequisites shouldContainExactly listOf(Prerequisite("PR 2", description = "PR 2 Desc"))
//    }
//
//    @Test
//    fun `multiple courses and prerequisites - all match`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C1"),
//        CourseEntity(name = "Course 2", identifier = "C2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllPrerequisites(
//        listOf(
//          NewPrerequisite(name = "PR 1", description = "PR 1 Desc", identifier = "C1"),
//          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
//          NewPrerequisite(name = "PR 3", description = "PR 3 Desc", identifier = "C2"),
//        ),
//      ).shouldBeEmpty()
//
//      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
//        "C1" to mutableSetOf(
//          Prerequisite("PR 1", "PR 1 Desc"),
//          Prerequisite("PR 2", "PR 2 Desc"),
//        ),
//        "C2" to mutableSetOf(
//          Prerequisite("PR 3", "PR 3 Desc"),
//        ),
//      )
//    }
//
//    @Test
//    fun `identifier mismatch - record ignored`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C1"),
//        CourseEntity(name = "Course 2", identifier = "C2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllPrerequisites(
//        listOf(
//          NewPrerequisite(name = "PR 1", description = "Don't care", identifier = "C1"),
//          NewPrerequisite(name = "PR 1", description = "Don't care", identifier = "CX"),
//          NewPrerequisite(name = "PR 2", description = "Don't care", identifier = "C2"),
//        ),
//      )
//        .shouldContainExactly(
//          LineMessage(
//            lineNumber = 3,
//            level = LineMessage.Level.error,
//            message = "No match for course identifier 'CX'",
//          ),
//        )
//    }
//
//    @Test
//    fun `NewPrerequisite has multiple identifiers`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C-1"),
//        CourseEntity(name = "Course 2", identifier = "C-2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllPrerequisites(
//        listOf(
//          NewPrerequisite(name = "PR 1", description = "D1", identifier = " C-1 , C-2 "),
//          NewPrerequisite(name = "PR 2", description = "D2", identifier = "C-2,C-X"),
//        ),
//      )
//
//      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
//        "C-1" to mutableSetOf(Prerequisite("PR 1", "D1")),
//        "C-2" to mutableSetOf(Prerequisite("PR 1", "D1"), Prerequisite("PR 2", "D2")),
//      )
//    }
  }
}
//
//  @Nested
//  @DisplayName("Replace All Offerings")
//  inner class ReplaceAllOfferingsTests {
//    @Test
//    fun `No records, No courses`() {
//      service.replaceAllOfferings(emptyList()).shouldBeEmpty()
//    }
//
//    @Test
//    fun `No records, one course that has offerings`() {
//      val allCourses = listOf(
//        CourseEntity(
//          name = "Course 1",
//          identifier = "C1",
//          description = "Description 1",
//          offerings = mutableSetOf(
//            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
//          ),
//        ),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllOfferings(emptyList()).shouldBeEmpty()
//
//      allCourses.flatMap { it.offerings }.shouldBeEmpty()
//    }
//
//    @Test
//    fun `One record matching one course that has offerings`() {
//      val allCourses = listOf(
//        CourseEntity(
//          name = "Course 1",
//          identifier = "C1",
//          offerings = mutableSetOf(
//            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
//          ),
//        ),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllOfferings(
//        listOf(
//          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
//        ),
//      ).shouldBeEmpty()
//
//      allCourses[0].offerings shouldContainExactly listOf(Offering(organisationId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"))
//    }
//
//    @Test
//    fun `multiple courses and offerings - all match`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C1"),
//        CourseEntity(name = "Course 2", identifier = "C2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllOfferings(
//        listOf(
//          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "admin@mdi.net"),
//          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
//          NewOffering(identifier = "C2", prisonId = "MDI", contactEmail = "admin@mdi.net"),
//        ),
//      ).shouldBeEmpty()
//
//      allCourses.associateBy(CourseEntity::name, CourseEntity::offerings) shouldBeEqual mapOf(
//        "Course 1" to mutableSetOf(
//          Offering(organisationId = "MDI", contactEmail = "admin@mdi.net"),
//          Offering(organisationId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
//        ),
//        "Course 2" to mutableSetOf(
//          Offering(organisationId = "MDI", contactEmail = "admin@mdi.net"),
//        ),
//      )
//    }
//
//    @Test
//    fun `identifier mismatch - record ignored`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C1"),
//        CourseEntity(name = "Course 2", identifier = "C2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllOfferings(
//        listOf(
//          NewOffering(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net"),
//          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
//          NewOffering(identifier = "CX", prisonId = "BWI", contactEmail = "x@y.net"),
//          NewOffering(identifier = "C2", prisonId = "MDI", contactEmail = "x@y.net"),
//        ),
//      )
//        .shouldContainExactly(
//          LineMessage(
//            lineNumber = 4,
//            level = LineMessage.Level.error,
//            message = "No course matches offering with identifier 'CX' and prisonId 'BWI'",
//          ),
//        )
//    }
//
//    @Test
//    fun `Missing contactEmail - Warning LineMessage produced`() {
//      val allCourses = listOf(
//        CourseEntity(name = "Course 1", identifier = "C1"),
//        CourseEntity(name = "Course 2", identifier = "C2"),
//      )
//      every { repository.allCourses() } returns allCourses
//
//      service.replaceAllOfferings(
//        listOf(
//          NewOffering(identifier = "C1", prisonId = "MDI"),
//          NewOffering(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
//        ),
//      )
//        .shouldContainExactly(
//          LineMessage(
//            lineNumber = 2,
//            level = LineMessage.Level.warning,
//            message = "Missing contactEmail for offering with identifier 'C1' at prisonId 'MDI'",
//          ),
//        )
//    }
//  }
