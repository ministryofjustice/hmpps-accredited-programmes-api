package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import java.util.UUID

class FakeRepository : CourseRepository {
  private val courses: MutableList<CourseEntity> = mutableListOf()
  private val audiences: MutableSet<Audience> = mutableSetOf()
  override fun allCourses(): List<CourseEntity> = courses.toList()
  override fun allActiveCourses(): List<CourseEntity> = allCourses().filterNot(CourseEntity::withdrawn)
  override fun course(courseId: UUID): CourseEntity? = TODO("Not yet implemented")
  override fun saveCourse(courseEntity: CourseEntity) {
    val persistentEntity = with(courseEntity) {
      CourseEntity(
        id = UUID.randomUUID(),
        identifier = identifier,
        name = name,
        description = description,
        alternateName = alternateName,
        withdrawn = withdrawn,
      ).also {
        it.audiences.addAll(audiences)
        it.prerequisites.addAll(prerequisites)
        it.offerings.addAll(offerings)
      }
    }

    courses.add(persistentEntity)
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
      service.updateCourses(listOf(CourseUpdate(name = "Course", identifier = "C", description = "Description", audience = "", alternateName = "CCC")))
      repository.allAudiences().shouldBeEmpty()
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToIgnoringFields(
        CourseEntity(
          name = "Course",
          alternateName = "CCC",
          identifier = "C",
          description = "Description",
        ),
        CourseEntity::id,
      )
    }

    @Test
    fun `Update one course`() {
      repository.saveCourse(CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description"))
      val persistentCourseId = repository.allCourses()[0].id!!

      service.updateCourses(listOf(CourseUpdate(name = "Updated Course", identifier = "C", description = "Updated Description", audience = "", alternateName = "UCCC")))

      repository.allAudiences().shouldBeEmpty()
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(id = persistentCourseId, name = "Updated Course", alternateName = "UCCC", identifier = "C", description = "Updated Description"),
      )
    }

    @Test
    fun `Withdraw one course`() {
      repository.saveCourse(CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description"))
      val persistentCourseId = repository.allCourses()[0].id!!
      service.updateCourses(listOf())

      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToComparingFields(
        CourseEntity(id = persistentCourseId, name = "Course", alternateName = "CCC", identifier = "C", description = "Description", withdrawn = true),
      )
    }

    @Test
    fun `Add one course with audiences`() {
      service.updateCourses(listOf(CourseUpdate(name = "Course", identifier = "C", description = "Description", audience = "A, B , C, ", alternateName = "CCC")))
      repository.allAudiences() shouldHaveSize 3
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToIgnoringFields(
        CourseEntity(
          name = "Course",
          alternateName = "CCC",
          identifier = "C",
          description = "Description",
        ).apply {
          audiences.addAll(setOf(Audience("A"), Audience("B"), Audience("C")))
        },
        CourseEntity::id,
      )
    }

    @Test
    fun `Update course audiences`() {
      service.updateCourses(listOf(CourseUpdate(name = "Course", identifier = "C", description = "Description", audience = "A, B, C", alternateName = "CCC")))

      repository.allAudiences().shouldContainExactly(Audience("A"), Audience("B"), Audience("C"))

      service.updateCourses(listOf(CourseUpdate(name = "Course", identifier = "C", description = "Description", audience = "A, D, ", alternateName = "CCC")))
      repository.allAudiences().shouldContainExactly(Audience("A"), Audience("B"), Audience("C"), Audience("D"))
      repository.allCourses() shouldHaveSize 1
      repository.allCourses()[0].shouldBeEqualToIgnoringFields(
        CourseEntity(name = "Course", alternateName = "CCC", identifier = "C", description = "Description").apply {
          audiences.addAll(setOf(Audience("A"), Audience("D")))
        },
        CourseEntity::id,
      )
    }
  }

  @Nested
  @DisplayName("Replace All Prerequisites")
  /**
   * The bulk upload of prerequisites deletes and re-inserts everything that is specified in the upload. This means
   * that the id on a prerequisite may change over time even if nothing about the prerequisite changes.
   * This is acceptable because nothing outside the course composite expects a prerequisite to have a long-term identity.
   */
  inner class ReplaceAllPrerequisitesTests {
    @Test
    fun `No records, No courses`() {
      service.updateAllPrerequisites(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `No records, one course that has prerequisites`() {
      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          prerequisites = mutableSetOf(
            Prerequisite(name = "PR 1", description = " PR Desc 1 "),
          ),
        ),
      )

      service.updateAllPrerequisites(emptyList()).shouldBeEmpty()
      repository.allCourses().flatMap { it.prerequisites }.shouldBeEmpty()
    }

    @Test
    fun `One record matching one course that has prerequisites`() {
      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          prerequisites = mutableSetOf(Prerequisite(name = "PR 1", description = " PR 1 Desc")),
        ),
      )

      service.updateAllPrerequisites(listOf(PrerequisiteUpdate(name = "PR 2", description = "PR 2 Desc", identifier = "C1"))).shouldBeEmpty()

      repository.allCourses()[0].prerequisites shouldContainExactly listOf(Prerequisite("PR 2", description = "PR 2 Desc"))
    }

    @Test
    fun `multiple courses and prerequisites - all match`() {
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C2"))

      service.updateAllPrerequisites(
        listOf(
          PrerequisiteUpdate(name = "PR 1", description = "PR 1 Desc", identifier = "C1"),
          PrerequisiteUpdate(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
          PrerequisiteUpdate(name = "PR 3", description = "PR 3 Desc", identifier = "C2"),
        ),
      ).shouldBeEmpty()

      repository.allCourses().associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
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
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C2"))

      service.updateAllPrerequisites(
        listOf(
          PrerequisiteUpdate(name = "PR 1", description = "Don't care", identifier = "C1"),
          PrerequisiteUpdate(name = "PR 1", description = "Don't care", identifier = "CX"),
          PrerequisiteUpdate(name = "PR 2", description = "Don't care", identifier = "C2"),
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
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C-1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C-2"))

      service.updateAllPrerequisites(
        listOf(
          PrerequisiteUpdate(name = "PR 1", description = "D1", identifier = " C-1 , C-2 "),
          PrerequisiteUpdate(name = "PR 2", description = "D2", identifier = "C-2,C-X"),
        ),
      )
      repository.allCourses().associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C-1" to mutableSetOf(Prerequisite("PR 1", "D1")),
        "C-2" to mutableSetOf(Prerequisite("PR 1", "D1"), Prerequisite("PR 2", "D2")),
      )
    }
  }

  @Nested
  @DisplayName("Update All Offerings")
  /**
   * Offerings must retain their id over their entire life-time because they are the subject of referrals.
   * This means that changes to offering properties (contactEmail, secondaryContactEmail) are applied as updates to
   * existing offerings.
   * An offering's organisationId is its business key and consequently never changes.
   */
  inner class UpdateAllOfferingsTests {
    @Test
    fun `No records, No courses`() {
      service.updateAllOfferings(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `updating offerings for an active course adds, deletes and updates properties as required`() {
      val offeringForB = Offering(organisationId = "B", contactEmail = "l@m.n")

      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C-1",
          withdrawn = false,
          offerings = mutableSetOf(Offering(organisationId = "A", contactEmail = "a@b.c"), offeringForB),
        ),
      )
      service.updateAllOfferings(
        listOf(
          OfferingUpdate(identifier = "C-1", prisonId = "B", contactEmail = "x@y.z"),
          OfferingUpdate(identifier = "C-1", prisonId = "C", contactEmail = "p@q.r"),
        ),
      )

      repository.allCourses()[0].offerings.shouldContainExactlyInAnyOrder(
        Offering(organisationId = "B", contactEmail = "x@y.z", id = offeringForB.id), // updated, not replaced.
        Offering(organisationId = "C", contactEmail = "p@q.r"),
      )
    }

    @Test
    fun `updating offerings for a withdrawn course has no effect`() {
      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C-1",
          withdrawn = true,
          offerings = mutableSetOf(
            Offering(organisationId = "A", contactEmail = "a@b.c"),
            Offering(organisationId = "B", contactEmail = "l@m.n"),
          ),
        ),
      )
      service.updateAllOfferings(
        listOf(
          OfferingUpdate(identifier = "C-1", prisonId = "B", contactEmail = "x@y.z"),
          OfferingUpdate(identifier = "C-1", prisonId = "C", contactEmail = "p@q.r"),
        ),
      )

      repository.allCourses()[0].offerings.shouldContainExactly(
        Offering(organisationId = "A", contactEmail = "a@b.c"),
        Offering(organisationId = "B", contactEmail = "l@m.n"),
      )
    }

    @Test
    fun `Update with no records removes offerings from an active course`() {
      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          offerings = mutableSetOf(
            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
          ),
        ),
      )

      service.updateAllOfferings(emptyList()).shouldBeEmpty()
      repository.allCourses().flatMap { it.offerings }.shouldBeEmpty()
    }

    @Test
    fun `Update with no records does not affect a withdrawn course`() {
      repository.saveCourse(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          withdrawn = true,
          offerings = mutableSetOf(
            Offering(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com"),
          ),
        ),
      )

      service.updateAllOfferings(emptyList()).shouldBeEmpty()

      repository.allCourses().flatMap { it.offerings } shouldHaveSize 1
    }

    @Test
    fun `updates for multiple courses and offerings target the correct courses`() {
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C2"))

      service.updateAllOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI", contactEmail = "admin@mdi.net"),
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
          OfferingUpdate(identifier = "C2", prisonId = "MDI", contactEmail = "admin@mdi.net"),
        ),
      ).shouldBeEmpty()

      repository.allActiveCourses().associateBy(CourseEntity::name, CourseEntity::offerings) shouldBeEqual mapOf(
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
    fun `identifier mismatches - records ignored`() {
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C2", withdrawn = true))

      service.updateAllOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "CX", prisonId = "BWI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "C2", prisonId = "MDI", contactEmail = "x@y.net"),
        ),
      ).shouldContainExactly(
        LineMessage(lineNumber = 4, level = LineMessage.Level.error, message = "No course matches offering with identifier 'CX' and prisonId 'BWI'"),
        LineMessage(lineNumber = 5, level = LineMessage.Level.error, message = "No course matches offering with identifier 'C2' and prisonId 'MDI'"),
      )
    }

    @Test
    fun `Missing contactEmail - Warning LineMessage produced`() {
      repository.saveCourse(CourseEntity(name = "Course 1", identifier = "C1"))
      repository.saveCourse(CourseEntity(name = "Course 2", identifier = "C2"))

      service.updateAllOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI"),
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
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
