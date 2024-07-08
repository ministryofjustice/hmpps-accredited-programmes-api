package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAllIgnoringFields
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseUpdateFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.NewPrerequisiteFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingUpdateFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PrerequisiteEntityFactory
import java.util.*

class CourseServiceTest {

  @MockK(relaxed = true)
  private lateinit var courseRepository: CourseRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: OfferingRepository

  private lateinit var courseService: CourseService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    courseService = CourseService(courseRepository, offeringRepository)
  }

  @Nested
  @DisplayName("Update Courses")
  inner class UpdateCoursesTests {

    @Test
    fun `updateCourses with one valid CourseEntity object clears and persists to the repository`() {
      val cu1 = CourseUpdateFactory().withIdentifier("C1").withAudience("A1").produce()
      val courseUpdates = listOf(cu1)

      courseService.updateCourses(courseUpdates)
      verify { courseRepository.saveAll<CourseEntity>(match { it.any { course -> course.identifier == "C1" } }) }
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
      val p1 = PrerequisiteEntityFactory().withName("P1").produce()
      val c1 = CourseEntityFactory().withIdentifier("C1").withPrerequisites(mutableSetOf(p1)).produce()
      val courses = listOf(c1)
      every { courseRepository.findAll() } returns courses

      courseService.updatePrerequisites(emptyList()).shouldBeEmpty()
      courses.flatMap { it.prerequisites }.shouldBeEmpty()
    }

    @Test
    fun `updatePrerequisites should replace existing prerequisites when a matching course is found`() {
      val prerequisite = PrerequisiteEntityFactory().withName("P1").withDescription("P1").produce()
      val course = CourseEntityFactory().withIdentifier("C1").withPrerequisites(mutableSetOf(prerequisite)).produce()
      val courses = listOf(course)
      every { courseRepository.findAll() } returns courses

      val newPrerequisite = NewPrerequisiteFactory().withName("P2").withDescription("P2").withIdentifier("C1").produce()
      courseService.updatePrerequisites(listOf(newPrerequisite)).shouldBeEmpty()

      val persistedNewPrerequisite = PrerequisiteEntityFactory().withName("P2").withDescription("P2").produce()
      courses[0].prerequisites shouldContainExactly listOf(persistedNewPrerequisite)
    }

    @Test
    fun `updatePrerequisites should associate multiple prerequisites to multiple courses when all identifiers match`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val np1 = NewPrerequisiteFactory().withName("P1").withDescription("P1").withIdentifier("C1").produce()
      val np2 = NewPrerequisiteFactory().withName("P2").withDescription("P2").withIdentifier("C1").produce()
      val np3 = NewPrerequisiteFactory().withName("P3").withDescription("P3").withIdentifier("C2").produce()
      val newPrerequisites = listOf(np1, np2, np3)

      courseService.updatePrerequisites(newPrerequisites).shouldBeEmpty()

      val persistedPrerequisite1 = PrerequisiteEntityFactory().withName("P1").withDescription("P1").produce()
      val persistedPrerequisite2 = PrerequisiteEntityFactory().withName("P2").withDescription("P2").produce()
      val persistedPrerequisite3 = PrerequisiteEntityFactory().withName("P3").withDescription("P3").produce()

      courses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C1" to mutableSetOf(persistedPrerequisite1, persistedPrerequisite2),
        "C2" to mutableSetOf(persistedPrerequisite3),
      )
    }

    @Test
    fun `updatePrerequisites should return an error when a prerequisite course identifier does matches no existing courses`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val np1 = NewPrerequisiteFactory().withName("P1").withIdentifier("C1").produce()
      val np2 = NewPrerequisiteFactory().withName("P2").withIdentifier("CX").produce()
      val np3 = NewPrerequisiteFactory().withName("P3").withIdentifier("C2").produce()
      val newPrerequisites = listOf(np1, np2, np3)

      val errorMessage = LineMessage(
        lineNumber = 3,
        level = LineMessage.Level.Error,
        message = "No match for course identifier 'CX'",
      )
      courseService.updatePrerequisites(newPrerequisites).shouldContainExactly(errorMessage)
    }

    @Test
    fun `updatePrerequisites should associate prerequisites to multiple courses when multiple matching identifiers are provided`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val np1 = NewPrerequisiteFactory().withName("P1").withDescription("P1").withIdentifier(" C1 , C2 ").produce()
      val np2 = NewPrerequisiteFactory().withName("P2").withDescription("P2").withIdentifier("C2,CX").produce()
      val newPrerequisites = listOf(np1, np2)

      courseService.updatePrerequisites(newPrerequisites)

      val persistedPrerequisite1 = PrerequisiteEntityFactory().withName("P1").withDescription("P1").produce()
      val persistedPrerequisite2 = PrerequisiteEntityFactory().withName("P2").withDescription("P2").produce()

      courses.associateBy(CourseEntity::identifier, CourseEntity::prerequisites) shouldBeEqual mapOf(
        "C1" to mutableSetOf(persistedPrerequisite1),
        "C2" to mutableSetOf(persistedPrerequisite1, persistedPrerequisite2),
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
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").produce()
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
        .apply { addOffering(o1) }
      val courses = listOf(c1)
      every { courseRepository.findAll() } returns courses

      courseService.updateOfferings(emptyList()).shouldBeEmpty()

      val updatedOfferings = courses.flatMap { it.offerings }
      updatedOfferings shouldHaveSize 1
      updatedOfferings[0] shouldBeSameInstanceAs o1
      o1.withdrawn shouldBe true
    }

    @Test
    fun `Given one record matching an offering from a course, updateOfferings should update that offering`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").produce()
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
        .apply { addOffering(o1) }
      val courses = listOf(c1)
      every { courseRepository.findAll() } returns courses

      val ou1 = OfferingUpdateFactory().withIdentifier("C1").withPrisonId("BWI").withContactEmail("bwi@c1.com").produce()
      val offeringUpdates = listOf(ou1)
      courseService.updateOfferings(offeringUpdates).shouldBeEmpty()

      val updatedOfferings = courses.flatMap { it.offerings }
      updatedOfferings shouldHaveSize 1

      val persistedOffering = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").produce()
      updatedOfferings[0] shouldBeSameInstanceAs o1
      o1.shouldBeEqualToIgnoringFields(persistedOffering, OfferingEntity::id, OfferingEntity::course)
    }

    @Test
    fun `Given one record that matches a course, but not the course's offering, then updateOfferings should withdraw the old offering and add the new offering`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").produce()
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
        .apply { addOffering(o1) }
      val courses = listOf(c1)
      every { courseRepository.findAll() } returns courses

      val ou1 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C1").withContactEmail("mdi@c1.com").produce()
      val offeringUpdates = listOf(ou1)
      courseService.updateOfferings(offeringUpdates).shouldBeEmpty()

      val persistedOffering1 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").withWithdrawn(true).produce()
      val persistedOffering2 = OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@c1.com").produce()

      val offeringsByOrganisationId = courses[0].offerings.associateBy(OfferingEntity::organisationId)
      offeringsByOrganisationId["BWI"]!!.shouldBeSameInstanceAs(o1)
      offeringsByOrganisationId["BWI"]!!.shouldBeEqualToIgnoringFields(persistedOffering1, OfferingEntity::id, OfferingEntity::course)
      offeringsByOrganisationId["MDI"]!!.shouldBeEqualToIgnoringFields(persistedOffering2, OfferingEntity::id, OfferingEntity::course)
    }

    @Test
    fun `Given multiple matching courses and offerings, updateOfferings should apply each offering to the correct course`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val ou1 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C1").withContactEmail("mdi@c1.com").produce()
      val ou2 = OfferingUpdateFactory().withPrisonId("BWI").withIdentifier("C1").withContactEmail("bwi@c1.com").produce()
      val ou3 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C2").withContactEmail("mdi@c2.com").produce()
      val offeringUpdates = listOf(ou1, ou2, ou3)
      courseService.updateOfferings(offeringUpdates).shouldBeEmpty()

      val persistedOffering1 = OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@c1.com").produce()
      val persistedOffering2 = OfferingEntityFactory().withOrganisationId("BWI").withContactEmail("bwi@c1.com").produce()
      val persistedOffering3 = OfferingEntityFactory().withOrganisationId("MDI").withContactEmail("mdi@c2.com").produce()

      val offeringsByCourseIdentifier = courses.associateBy(CourseEntity::identifier, CourseEntity::offerings)
      offeringsByCourseIdentifier["C1"]!!.shouldContainAllIgnoringFields(mutableSetOf(persistedOffering1, persistedOffering2), OfferingEntity::id, OfferingEntity::course)
      offeringsByCourseIdentifier["C2"]!!.shouldContainAllIgnoringFields(mutableSetOf(persistedOffering3), OfferingEntity::id, OfferingEntity::course)
    }

    @Test
    fun `updateOfferings should return an error when an offering course identifier does not match any existing courses`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val ou1 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C1").withContactEmail("mdi@c1.com").produce()
      val ou2 = OfferingUpdateFactory().withPrisonId("BWI").withIdentifier("C1").withContactEmail("bwi@c1.com").produce()
      val ou3 = OfferingUpdateFactory().withPrisonId("BWI").withIdentifier("CX").withContactEmail("bwi@cx.com").produce()
      val ou4 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C2").withContactEmail("mdi@c2.com").produce()
      val offeringUpdates = listOf(ou1, ou2, ou3, ou4)

      val errorMessage = LineMessage(
        lineNumber = 4,
        level = LineMessage.Level.Error,
        message = "No course matches offering with identifier 'CX' and prisonId 'BWI'",
      )
      courseService.updateOfferings(offeringUpdates).shouldContainExactly(errorMessage)
    }

    @Test
    fun `updateOfferings should return a warning when the contact email for an offering is missing`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses

      val ou1 = OfferingUpdateFactory().withPrisonId("MDI").withIdentifier("C1").produce()
      val ou2 = OfferingUpdateFactory().withPrisonId("BWI").withIdentifier("C1").withContactEmail("bwi@c1.com").produce()
      val offeringUpdates = listOf(ou1, ou2)

      val errorMessage = LineMessage(
        lineNumber = 2,
        level = LineMessage.Level.Warning,
        message = "Missing contactEmail for offering with identifier 'C1' at prisonId 'MDI'",
      )
      courseService.updateOfferings(offeringUpdates).shouldContainExactly(errorMessage)
    }
  }

  @Nested
  @DisplayName("Handle withdrawn Offerings")
  inner class WithdrawnOfferingsTests {
    @Test
    fun `Withdrawn offerings should not be returned from getAllOfferingsByCourseId`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
      val o2 = OfferingEntityFactory().withOrganisationId("MDI").produce()
      val offerings = listOf(o1, o2)
      every { offeringRepository.findAllByCourseId(any()) } returns offerings

      courseService.getAllOfferingsByCourseId(UUID.randomUUID()).shouldContainExactly(o2)
    }

    @Test
    fun `A withdrawn Offering should not be returned from getOfferingById`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
      every { offeringRepository.findById(any()) } returns Optional.of(o1)

      courseService.getOfferingById(UUID.randomUUID()).shouldBeNull()
    }

    @Test
    fun `An active Offering should be returned from getOfferingById`() {
      val o1 = OfferingEntityFactory().withOrganisationId("MDI").produce()
      every { offeringRepository.findById(any()) } returns Optional.of(o1)

      courseService.getOfferingById(UUID.randomUUID()) shouldBe o1
    }
  }

  @Nested
  @DisplayName("Handle withdrawn CourseEntities")
  inner class WithdrawnCourseTests {
    @Test
    fun `A withdrawn course should not be returned from getCourseById`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").withWithdrawn(true).produce()
      every { courseRepository.findById(any()) } returns Optional.of(c1)
      courseService.getNotWithdrawnCourseById(UUID.randomUUID()).shouldBeNull()
    }

    @Test
    fun `An active course should  be returned from getCourseById`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").produce()
      every { courseRepository.findById(any()) } returns Optional.of(c1)
      courseService.getNotWithdrawnCourseById(UUID.randomUUID()) shouldBe c1
    }

    @Test
    fun `getAllCourses should exclude withdrawn courses`() {
      val c1 = CourseEntityFactory().withIdentifier("C1").withWithdrawn(true).produce()
      val c2 = CourseEntityFactory().withIdentifier("C2").produce()
      val courses = listOf(c1, c2)
      every { courseRepository.findAll() } returns courses
      courseService.getAllCourses().shouldContainExactly(c2)
    }
  }

  @Nested
  @DisplayName("Get Offerings by organisationId")
  inner class GetOfferingsByOrganisationId {
    @Test
    fun `should return empty list when no offerings exist for an organisationId`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
      val offerings = listOf(o1)
      every { offeringRepository.findAll() } returns offerings
      courseService.getAllOfferingsByOrganisationId("xxx").shouldBeEmpty()
    }

    @Test
    fun `should return only offerings for requested organisationID`() {
      val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
      val o2 = OfferingEntityFactory().withOrganisationId("MDI").produce()
      val offerings = listOf(o1, o2)
      every { offeringRepository.findAll() } returns offerings
      courseService.getAllOfferingsByOrganisationId(o1.organisationId).shouldContainExactly(o1)
    }
  }
}
