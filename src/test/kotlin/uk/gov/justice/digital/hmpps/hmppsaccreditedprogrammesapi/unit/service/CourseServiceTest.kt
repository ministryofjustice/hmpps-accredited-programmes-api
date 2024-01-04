package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LineMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AudienceRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.eqCourse
import java.util.*

class CourseServiceTest {

  @MockK(relaxed = true)
  private lateinit var courseRepository: CourseRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: OfferingRepository

  @MockK(relaxed = true)
  private lateinit var audienceRepository: AudienceRepository

  private lateinit var courseService: CourseService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    courseService = CourseService(courseRepository, offeringRepository, audienceRepository)
  }

  @Nested
  @DisplayName("Update Courses")
  inner class UpdateCoursesTests {
    @Test
    fun `updateCourses with an empty list clears the audience repository`() {
      courseService.updateCourses(emptyList())
      verify { audienceRepository.saveAll(emptySet()) }
    }

    @Test
    fun `updateCourses with one valid CourseEntity object clears and persists to the repository`() {
      val a1 = AudienceEntity("Audience 1", id = UUID.randomUUID())

      every { audienceRepository.findAll() } returns listOf(a1)

      courseService.updateCourses(
        listOf(
          CourseUpdate(name = "Course", identifier = "C", description = "Description", audience = "Audience 1", alternateName = "CCC", referable = true),
        ),
      )

      verify {
        courseRepository.save(eqCourse(CourseEntity(name = "Course", identifier = "C", description = "Description", audiences = mutableSetOf(a1), referable = true)))
      }
    }

    @Test
    fun `updateCourses with two valid CourseEntity objects clears and persists to the repository`() {
      val a1 = AudienceEntity("Audience 1", id = UUID.randomUUID())
      val a2 = AudienceEntity("Audience 2", id = UUID.randomUUID())
      val a3 = AudienceEntity("Audience 3", id = UUID.randomUUID())

      every { audienceRepository.findAll() } returns listOf(a1, a2, a3)

      courseService.updateCourses(
        listOf(
          CourseUpdate(name = "Course 1", identifier = "C1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", alternateName = "111", referable = true),
          CourseUpdate(name = "Course 2", identifier = "C2", description = "Description 2", audience = "${a1.value}, ${a3.value}", alternateName = "222", referable = true),
        ),
      )
      verify { courseRepository.save(eqCourse(CourseEntity(name = "Course 1", identifier = "C1", description = "Description 1", audiences = mutableSetOf(a1, a2), referable = true))) }
      verify { courseRepository.save(eqCourse(CourseEntity(name = "Course 2", identifier = "C2", description = "Description 2", audiences = mutableSetOf(a1, a3), referable = true))) }
    }

    @Test
    fun `updateCourses with duplicate AudienceEntity values only persists unique values to the repository`() {
      val a1 = AudienceEntity("Audience 1", id = UUID.randomUUID())
      val a2 = AudienceEntity("Audience 2", id = UUID.randomUUID())
      val a3 = AudienceEntity("Audience 3", id = UUID.randomUUID())

      every { audienceRepository.findAll() } returns listOf()

      courseService.updateCourses(
        listOf(
          CourseUpdate(name = "Course 1", identifier = "C1", description = "Description 1", audience = "${a1.value}, ${a2.value} ", alternateName = "111"),
          CourseUpdate(name = "Course 2", identifier = "C2", description = "Description 2", audience = "${a1.value}, ${a3.value}", alternateName = "222"),
          CourseUpdate(name = "Course 3", identifier = "C3", description = "Description 3", audience = a1.value, alternateName = "333"),
          CourseUpdate(name = "Course 4", identifier = "C4", description = "Description 4", audience = a1.value, alternateName = "444"),
        ),
      )

      verify { audienceRepository.saveAll(setOf(AudienceEntity(a1.value), AudienceEntity(a2.value), AudienceEntity(a3.value))) }
    }
  }

  @Nested
  @DisplayName("Update Prerequisites")
  inner class ReplaceAllPrerequisitesTests {
    @Test
    fun `updatePrerequisites with an empty list successfully clears the repository`() {
      courseService.updatePrerequisites(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `updatePrerequisites should remove existing prerequisites when no new records are provided`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
          prerequisites = mutableSetOf(
            PrerequisiteEntity(name = "PR 1", description = " PR Desc 1 "),
          ),
        ),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updatePrerequisites(emptyList()).shouldBeEmpty()

      allCourses.flatMap { it.prerequisites }.shouldBeEmpty()
    }

    @Test
    fun `updatePrerequisites should replace existing prerequisites when a matching course is found`() {
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          prerequisites = mutableSetOf(PrerequisiteEntity(name = "PR 1", description = " PR 1 Desc")),
        ),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updatePrerequisites(
        listOf(
          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
        ),
      ).shouldBeEmpty()

      allCourses[0].prerequisites shouldContainExactly listOf(PrerequisiteEntity(name = "PR 2", description = "PR 2 Desc"))
    }

    @Test
    fun `updatePrerequisites should associate multiple prerequisites to multiple courses when all identifiers match`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updatePrerequisites(
        listOf(
          NewPrerequisite(name = "PR 1", description = "PR 1 Desc", identifier = "C1"),
          NewPrerequisite(name = "PR 2", description = "PR 2 Desc", identifier = "C1"),
          NewPrerequisite(name = "PR 3", description = "PR 3 Desc", identifier = "C2"),
        ),
      ).shouldBeEmpty()

      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C1" to mutableSetOf(
          PrerequisiteEntity(name = "PR 1", description = "PR 1 Desc"),
          PrerequisiteEntity(name = "PR 2", description = "PR 2 Desc"),
        ),
        "C2" to mutableSetOf(
          PrerequisiteEntity(name = "PR 3", description = "PR 3 Desc"),
        ),
      )
    }

    @Test
    fun `updatePrerequisites should return an error when a prerequisite course identifier does matches no existing courses`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updatePrerequisites(
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
    fun `updatePrerequisites should associate prerequisites to multiple courses when multiple matching identifiers are provided`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C-1"),
        CourseEntity(name = "Course 2", identifier = "C-2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updatePrerequisites(
        listOf(
          NewPrerequisite(name = "PR 1", description = "D1", identifier = " C-1 , C-2 "),
          NewPrerequisite(name = "PR 2", description = "D2", identifier = "C-2,C-X"),
        ),
      )

      allCourses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C-1" to mutableSetOf(PrerequisiteEntity(name = "PR 1", description = "D1")),
        "C-2" to mutableSetOf(PrerequisiteEntity(name = "PR 1", description = "D1"), PrerequisiteEntity(name = "PR 2", description = "D2")),
      )
    }
  }

  @Nested
  @DisplayName("Update Offerings")
  inner class UpdateOfferingsTests {
    @Test
    fun `updateOfferings with an empty list clears the repository`() {
      courseService.updateOfferings(emptyList()).shouldBeEmpty()
    }

    @Test
    fun `Given no records and one course that has an offering, updateOfferings should withdraw that offering`() {
      val theOffering = OfferingEntity(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com")

      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
          description = "Description 1",
        ).apply {
          this.id?.let { courseService.addOfferingToCourse(it, theOffering) }
        },
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(emptyList()).shouldBeEmpty()

      val updatedOfferings = allCourses.flatMap { it.mutableOfferings }
      updatedOfferings shouldHaveSize 1
      updatedOfferings[0] shouldBeSameInstanceAs theOffering
      theOffering.withdrawn shouldBe true
    }

    @Test
    fun `Given one record matching an offering from a course, updateOfferings should update that offering`() {
      val theOffering = OfferingEntity(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com")
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
        ).apply {
          this.id?.let { courseService.addOfferingToCourse(it, theOffering) }
        },
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
        ),
      ).shouldBeEmpty()

      val updatedOfferings = allCourses.flatMap { it.mutableOfferings }
      updatedOfferings shouldHaveSize 1
      updatedOfferings[0] shouldBeSameInstanceAs theOffering
      theOffering.shouldBeEqualToIgnoringFields(
        OfferingEntity(organisationId = "BWI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
        OfferingEntity::course,
      )
    }

    @Test
    fun `Given one record that matches a course, but not the course's offering, then updateOfferings should withdraw the old offering and add the new offering`() {
      val oldOffering = OfferingEntity(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com")
      val allCourses = listOf(
        CourseEntity(
          name = "Course 1",
          identifier = "C1",
        ).apply {
          this.id?.let { courseService.addOfferingToCourse(it, oldOffering) }
        },
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
        ),
      ).shouldBeEmpty()

      val offeringsByOrganisationId = allCourses[0].mutableOfferings.associateBy(OfferingEntity::organisationId)

      offeringsByOrganisationId["MDI"]!!.shouldBeEqualToIgnoringFields(
        OfferingEntity(organisationId = "MDI", contactEmail = "x@y.net", secondaryContactEmail = "z@y.net"),
        OfferingEntity::course,
      )

      offeringsByOrganisationId["BWI"]!!.shouldBeSameInstanceAs(oldOffering)
      offeringsByOrganisationId["BWI"]!!.shouldBeEqualToIgnoringFields(
        OfferingEntity(organisationId = "BWI", contactEmail = "a@b.com", secondaryContactEmail = "c@b.com", withdrawn = true),
        OfferingEntity::course,
      )
    }

    @Test
    fun `Given multiple matching courses and offerings, updateOfferings should associate the courses to their offerings`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI", contactEmail = "admin@mdi.net"),
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
          OfferingUpdate(identifier = "C2", prisonId = "MDI", contactEmail = "admin@mdi.net"),
        ),
      ).shouldBeEmpty()

      allCourses.associateBy(CourseEntity::name, CourseEntity::mutableOfferings) shouldBeEqual mapOf(
        "Course 1" to mutableSetOf(
          OfferingEntity(organisationId = "MDI", contactEmail = "admin@mdi.net"),
          OfferingEntity(organisationId = "BWI", contactEmail = "admin@bwi.net", secondaryContactEmail = "admin2@bwi.net"),
        ),
        "Course 2" to mutableSetOf(
          OfferingEntity(organisationId = "MDI", contactEmail = "admin@mdi.net"),
        ),
      )
    }

    @Test
    fun `updateOfferings should return an error when an offering course identifier does not match any existing courses`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(
        listOf(
          OfferingUpdate(identifier = "C1", prisonId = "MDI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "C1", prisonId = "BWI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "CX", prisonId = "BWI", contactEmail = "x@y.net"),
          OfferingUpdate(identifier = "C2", prisonId = "MDI", contactEmail = "x@y.net"),
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
    fun `updateOfferings should return a warning when the contact email for an offering is missing`() {
      val allCourses = listOf(
        CourseEntity(name = "Course 1", identifier = "C1"),
        CourseEntity(name = "Course 2", identifier = "C2"),
      )
      every { courseRepository.findAll() } returns allCourses

      courseService.updateOfferings(
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

  @Nested
  @DisplayName("Handle withdrawn Offerings")
  inner class WithdrawnOfferingsTests {
    @Test
    fun `Withdrawn offerings should not be returned from getAllOfferingsByCourseId`() {
      val withdrawnOffering = OfferingEntity(withdrawn = true, organisationId = "BWI", contactEmail = "a@b.com")
      val offering = OfferingEntity(organisationId = "MDI", contactEmail = "a@b.com")

      every { offeringRepository.findAllByCourseId(any()) } returns listOf(withdrawnOffering, offering)

      courseService.getAllOfferingsByCourseId(UUID.randomUUID()).shouldContainExactly(offering)
    }

    @Test
    fun `A withdrawn Offering should not be returned from getOfferingById`() {
      val withdrawnOffering = OfferingEntity(withdrawn = true, organisationId = "BWI", contactEmail = "a@b.com")

      every { offeringRepository.findById(any()) } returns Optional.of(withdrawnOffering)

      courseService.getOfferingById(UUID.randomUUID()).shouldBeNull()
    }

    @Test
    fun `An active Offering should be returned from getOfferingById`() {
      val offering = OfferingEntity(organisationId = "MDI", contactEmail = "a@b.com")

      every { offeringRepository.findById(any()) } returns Optional.of(offering)

      courseService.getOfferingById(UUID.randomUUID()) shouldBe offering
    }
  }

  @Nested
  @DisplayName("Handle withdrawn CourseEntities")
  inner class WithdrawnCourseTests {
    @Test
    fun `A withdrawn course should not be returned from getCourseById`() {
      val withdrawnCourse = CourseEntity(name = "Course", identifier = "C", withdrawn = true)
      every { courseRepository.findById(any()) } returns Optional.of(withdrawnCourse)
      courseService.getCourseById(UUID.randomUUID()).shouldBeNull()
    }

    @Test
    fun `An active course should  be returned from getCourseById`() {
      val activeCourse = CourseEntity(name = "Course", identifier = "C")
      every { courseRepository.findById(any()) } returns Optional.of(activeCourse)
      courseService.getCourseById(UUID.randomUUID()) shouldBe activeCourse
    }

    @Test
    fun `getAllCourses should exclude withdrawn courses`() {
      val withdrawnCourse = CourseEntity(name = "Withdrawn", identifier = "W", withdrawn = true)
      val activeCourse = CourseEntity(name = "Active", identifier = "A")
      every { courseRepository.findAll() } returns listOf(activeCourse, withdrawnCourse)
      courseService.getAllCourses().shouldContainExactly(activeCourse)
    }
  }

  @Nested
  @DisplayName("Get Offerings by organisationId")
  inner class GetOfferingsByOrganisationId {

    @Test
    fun `should return empty list when no offerings exist for an organisationId`() {
      val offering = OfferingEntity(withdrawn = true, organisationId = "BWI", contactEmail = "a@b.com")

      every { offeringRepository.findAll() } returns listOf(offering)

      courseService.getAllOfferingsByOrganisationId("xxx").isEmpty()
    }

    @Test
    fun `should return only offerings for requested organisationID`() {
      val offering1 = OfferingEntity(withdrawn = true, organisationId = "BWI", contactEmail = "a@b.com")
      val offering2 = OfferingEntity(organisationId = "MDI", contactEmail = "a@b.com")

      every { offeringRepository.findAll() } returns listOf(offering1, offering2)

      courseService.getAllOfferingsByOrganisationId(offering1.organisationId).shouldContainExactly(offering1)
    }
  }
}
