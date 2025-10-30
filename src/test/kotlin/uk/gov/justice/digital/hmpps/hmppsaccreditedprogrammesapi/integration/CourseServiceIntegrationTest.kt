package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CourseServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var courseRepository: CourseRepository

  @Autowired
  lateinit var courseService: CourseService

  @Autowired
  lateinit var offeringRepository: OfferingRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createCourse(
      CourseEntityFactory()
        .withName("Building Choices: high intensity")
        .withAudience("General offence").produce(),
    )
    val course = courseRepository.findAllByName("Building Choices: high intensity").first()
    persistenceHelper.createOffering(
      OfferingEntityFactory()
        .withCourse(course)
        .withOrganisationId("LPI").produce(),
    )
  }

  @Test
  fun `should update course offering to withdrawn and not referable`() {
    // Given - use data for Building Choices: high intensity from migration scripts
    val buildingChoicesCourse = courseRepository.findAllByName("Building Choices: high intensity")
      .first { it.audience == "General offence" }
    val offering = offeringRepository.findAllByCourseId(buildingChoicesCourse.id!!)
      .first { it.organisationId == "LPI" }

    val courseOffering = CourseOffering(
      id = offering.id,
      organisationId = "LPI",
      contactEmail = "a@b.com",
      withdrawn = true,
      referable = false,
    )

    // When
    val updatedCourseOffering = courseService.updateOffering(buildingChoicesCourse.id!!, courseOffering)

    // Then
    assertThat(updatedCourseOffering).isNotNull
    assertThat(updatedCourseOffering.withdrawn).isTrue
    assertThat(updatedCourseOffering.referable).isFalse
  }

  @Test
  fun `should update course offering to allow a withdrawn offering to be re-enabled`() {
    // Given - existing actual data for Building Choices: high intensity
    val buildingChoicesCourse = courseRepository.findAllByName("Building Choices: high intensity")
      .first { it.audience == "General offence" }
    val offering = offeringRepository.findAllByCourseId(buildingChoicesCourse.id!!)
      .first { it.organisationId == "LPI" }

    val courseOffering = CourseOffering(
      id = offering.id,
      organisationId = "LPI",
      contactEmail = "a@b.com",
      withdrawn = true,
      referable = false,
    )

    // When
    courseService.updateOffering(buildingChoicesCourse.id!!, courseOffering)
    // Attempt to re-activate course offering
    val updatedCourseOffering = courseService.updateOffering(
      buildingChoicesCourse.id!!,
      courseOffering.copy(withdrawn = false, referable = true),
    )

    // Then
    assertThat(updatedCourseOffering).isNotNull
    assertThat(updatedCourseOffering.withdrawn).isFalse
    assertThat(updatedCourseOffering.referable).isTrue
  }
}
