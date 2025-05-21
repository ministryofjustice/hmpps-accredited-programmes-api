package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.CourseVariantEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseVariantRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PniService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
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

  @MockK(relaxed = true)
  private lateinit var organisationService: OrganisationService

  @MockK(relaxed = true)
  private lateinit var courseVariantRepository: CourseVariantRepository

  @MockK(relaxed = true)
  private lateinit var pniService: PniService

  private lateinit var courseService: CourseService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    courseService = CourseService(
      courseRepository,
      offeringRepository,
      prisonRegisterApiService,
      referralRepository,
      organisationService,
      pniService,
      courseVariantRepository,
    )
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
        // Given
        val o1 = OfferingEntityFactory().withOrganisationId("BWI").produce()
        val offerings = listOf(o1)
        every { offeringRepository.findOfferingsByOrganisationIdWithActiveReferrals(o1.organisationId) } returns offerings
        // When & Then
        courseService.getAllOfferingsByOrganisationId(o1.organisationId).shouldContainOnly(o1)
      }
    }
  }

  @Test
  fun `getBuildingChoicesCourseIntensity should return high intensity for HIGH_INTENSITY_BC`() {
    val result = courseService.getIntensityOfBuildingChoicesCourse("HIGH_INTENSITY_BC")
    result shouldBe "high intensity"
  }

  @Test
  fun `getBuildingChoicesCourseIntensity should return moderate intensity for MODERATE_INTENSITY_BC`() {
    val result = courseService.getIntensityOfBuildingChoicesCourse("MODERATE_INTENSITY_BC")
    result shouldBe "moderate intensity"
  }

  @Test
  fun `getBuildingChoicesCourseIntensity should throw BusinessException for unknown pathway`() {
    val exception = shouldThrow<BusinessException> {
      courseService.getIntensityOfBuildingChoicesCourse("UNKNOWN_PATHWAY")
    }
    exception.message shouldBe "Building choices course could not be found for programmePathway UNKNOWN_PATHWAY"
  }

  @Test
  fun `getBuildingChoicesCourses should return list of CourseEntity`() {
    val courseId1 = UUID.randomUUID()
    val courseId2 = UUID.randomUUID()
    val variantCourseId1 = UUID.randomUUID()
    val variantCourseId2 = UUID.randomUUID()

    val courseVariantEntities = listOf(
      CourseVariantEntity(courseId = courseId1, variantCourseId = variantCourseId1),
      CourseVariantEntity(courseId = courseId2, variantCourseId = variantCourseId2),
    )

    val courseEntities = listOf(
      CourseEntityFactory().withId(courseId1).produce(),
      CourseEntityFactory().withId(courseId2).produce(),
      CourseEntityFactory().withId(variantCourseId1).produce(),
      CourseEntityFactory().withId(variantCourseId2).produce(),
    )

    every { courseVariantRepository.findAll() } returns courseVariantEntities
    every { courseRepository.findAllById(any()) } returns courseEntities

    val result = courseService.getAllBuildingChoicesCourses()

    result.size shouldBe 4
    result.map { it.id } shouldContainOnly listOf(courseId1, courseId2, variantCourseId1, variantCourseId2)
  }

  @Test
  fun `get building choices course for transferring a given referral returns course with the appropriate audience`() {
    // Given
    val courseId1 = UUID.randomUUID()
    val courseId2 = UUID.randomUUID()
    val variantCourseId1 = UUID.randomUUID()
    val variantCourseId2 = UUID.randomUUID()

    val courseVariantEntities = listOf(
      CourseVariantEntity(courseId = courseId1, variantCourseId = variantCourseId1),
      CourseVariantEntity(courseId = courseId2, variantCourseId = variantCourseId2),
    )

    val organisationId = "WTI"
    val courseEntities = listOf(
      CourseEntityFactory().withId(courseId1).withName("Building Choices: high intensity").withAudience("General offence").produce(),
      CourseEntityFactory().withId(courseId2).withName("Building Choices: moderate intensity").withAudience("Sexual offence").withOfferings(
        mutableSetOf(OfferingEntityFactory().withWithdrawn(false).withOrganisationId(organisationId).produce()),
      ).produce(),
      CourseEntityFactory().withId(variantCourseId1).produce(),
      CourseEntityFactory().withId(variantCourseId2).produce(),
    )

    val referral = ReferralEntityFactory().withOffering(OfferingEntityFactory().withOrganisationId(organisationId).withCourse(CourseEntityFactory().withWithdrawn(false).withAudience("Sexual offence").produce()).produce()).produce()
    every { referralRepository.findById(referral.id!!) } returns Optional.of(referral)
    every { courseVariantRepository.findAll() } returns courseVariantEntities
    every { courseRepository.findAllById(any()) } returns courseEntities
    every { organisationService.findOrganisationEntityByCode(any()) } returns OrganisationEntityFactory()
      .withCode(organisationId)
      .withName("Whatton")
      .withGender(Gender.MALE).produce()
    // When
    val recommendedBuildingChoicesCourse = courseService.getBuildingChoicesCourseForTransferringReferral(referral.id!!, "MODERATE_INTENSITY_BC")

    // Then
    recommendedBuildingChoicesCourse.id shouldBe courseId2
    recommendedBuildingChoicesCourse.name shouldBe "Building Choices: moderate intensity"
    recommendedBuildingChoicesCourse.audience shouldBe "Sexual offence"
    recommendedBuildingChoicesCourse.courseOfferings.size shouldBe 1
    recommendedBuildingChoicesCourse.courseOfferings.first().organisationId shouldBe organisationId
  }

  @Test
  fun `should return building choices course with general offence as audience when offence is different to sexual offence`() {
    val courseId1 = UUID.randomUUID()
    val courseId2 = UUID.randomUUID()
    val variantCourseId1 = UUID.randomUUID()
    val variantCourseId2 = UUID.randomUUID()

    val courseVariantEntities = listOf(
      CourseVariantEntity(courseId = courseId1, variantCourseId = variantCourseId1),
      CourseVariantEntity(courseId = courseId2, variantCourseId = variantCourseId2),
    )

    val organisationId = "WTI"
    val courseEntities = listOf(
      CourseEntityFactory().withId(courseId1).withName("Building Choices: high intensity").withAudience("General offence").withOfferings(
        mutableSetOf(OfferingEntityFactory().withWithdrawn(false).withOrganisationId(organisationId).produce()),
      ).produce(),
      CourseEntityFactory().withId(courseId2).withName("Building Choices: moderate intensity").withAudience("Sexual offence").produce(),
      CourseEntityFactory().withId(variantCourseId1).produce(),
      CourseEntityFactory().withId(variantCourseId2).produce(),
    )

    val referral = ReferralEntityFactory().withOffering(OfferingEntityFactory().withOrganisationId(organisationId).withCourse(CourseEntityFactory().withWithdrawn(false).withAudience("Intimate partner violence offence").produce()).produce()).produce()
    every { referralRepository.findById(referral.id!!) } returns Optional.of(referral)
    every { courseVariantRepository.findAll() } returns courseVariantEntities
    every { courseRepository.findAllById(any()) } returns courseEntities
    every { organisationService.findOrganisationEntityByCode(any()) } returns OrganisationEntityFactory().withCode(
      organisationId,
    ).withName("Whatton").withGender(Gender.MALE).produce()
    val recommendedBuildingChoicesCourse = courseService.getBuildingChoicesCourseForTransferringReferral(referral.id!!, "HIGH_INTENSITY_BC")

    recommendedBuildingChoicesCourse.id shouldBe courseId1
    recommendedBuildingChoicesCourse.name shouldBe "Building Choices: high intensity"
    recommendedBuildingChoicesCourse.audience shouldBe "General offence"
    recommendedBuildingChoicesCourse.courseOfferings.size shouldBe 1
    recommendedBuildingChoicesCourse.courseOfferings.first().organisationId shouldBe organisationId
  }

  @Test
  fun `should return building choices course variants successful`() {
    val courseId1 = UUID.randomUUID()
    val courseId2 = UUID.randomUUID()
    val variantCourseId1 = UUID.randomUUID()
    val variantCourseId2 = UUID.randomUUID()

    val courseVariantEntities = listOf(
      CourseVariantEntity(courseId = courseId1, variantCourseId = variantCourseId1),
      CourseVariantEntity(courseId = courseId2, variantCourseId = variantCourseId2),
    )

    every { courseVariantRepository.findAllByCourseId(courseId1) } returns courseVariantEntities.find { it.courseId == courseId1 }
    every { courseRepository.findBuildingChoicesCourses(any(), any(), any()) } returns listOf(CourseEntityFactory().withName("Building Choices: high intensity").produce())

    val buildingChoicesCourseVariants = courseService.getBuildingChoicesCourseVariants(
      courseId1,
      isInAWomensPrison = false,
      isConvictedOfASexualOffence = false,
    )

    buildingChoicesCourseVariants?.size shouldBe 1
    buildingChoicesCourseVariants?.first()?.name shouldBe "Building Choices: high intensity"
  }
}
