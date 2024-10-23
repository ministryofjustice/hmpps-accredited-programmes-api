package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import java.util.*

class CourseServiceTest {

  @MockK(relaxed = true)
  private lateinit var courseRepository: CourseRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: OfferingRepository

  @MockK(relaxed = true)
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @MockK(relaxed = true)
  private lateinit var referralRepository: ReferralRepository

  private lateinit var courseService: CourseService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    courseService = CourseService(courseRepository, offeringRepository, prisonRegisterApiService, referralRepository)
  }

  @Nested
  @DisplayName("Handle withdrawn Offerings")
  inner class WithdrawnOfferingsTests {
    @Test
    fun `Withdrawn offerings should not be returned from getAllOfferingsByCourseId`() {
      fun `Withdrawn offerings should not be returned from getAllOfferings when withdrawn is false`() {
        val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
        val o2 = OfferingEntityFactory().withOrganisationId("MDI").produce()

        every { offeringRepository.findAllByCourseIdAndWithdrawnIsFalse(any()) } returns listOf(o2)

        courseService.getAllOfferings(UUID.randomUUID()).shouldContainExactly(o2)
      }

      @Test
      fun `All offerings should be returned from getAllOfferings when withdrawn is true`() {
        val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
        val o2 = OfferingEntityFactory().withOrganisationId("MDI").produce()
        val offerings = listOf(o1, o2)
        every { offeringRepository.findAllByCourseId(any()) } returns offerings

        courseService.getAllOfferings(UUID.randomUUID(), true).shouldContainExactly(o1, o2)
      }

      @Test
      fun `Withdrawn offerings returned from getAllOfferingsByCourseId when include withdrawn flag is set to true`() {
        val o1 = OfferingEntityFactory().withOrganisationId("BWI").withWithdrawn(true).produce()
        val o2 = OfferingEntityFactory().withOrganisationId("MDI").produce()
        val offerings = listOf(o1, o2)
        every { offeringRepository.findAllByCourseId(any()) } returns offerings

        courseService.getAllOfferings(UUID.randomUUID(), true).shouldContainExactly(offerings)
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
        every { courseRepository.findAllByWithdrawnIsFalse() } returns listOf(c2)
        courseService.getAllCourses().shouldContainExactly(c2)
      }

      @Test
      fun `getAllCourses should return all courses when includeWithdrawn courses is true`() {
        val c1 = CourseEntityFactory().withIdentifier("C1").withWithdrawn(true).produce()
        val c2 = CourseEntityFactory().withIdentifier("C2").produce()
        val courseEntities = listOf(c1, c2)
        every { courseRepository.findAll() } returns courseEntities
        courseService.getAllCourses(true).shouldContainExactly(courseEntities)
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
}
