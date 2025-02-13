package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import java.lang.IllegalStateException
import java.util.*

@ExtendWith(MockitoExtension::class)
class OfferingServiceTest {

  @Mock
  private lateinit var courseService: CourseService

  @Mock
  private lateinit var offeringRepository: OfferingRepository

  @InjectMocks
  private lateinit var offeringService: OfferingService

  @Test
  fun `should throw IllegalStateException when no matching courses are found`() {
    // Given
    val courseIntensity = "MEDIUM"
    val courseAudience = "General Offence"
    val organisationId = "HIK"

    whenever(courseService.getBuildingChoicesCourses()).thenReturn(emptyList())

    // When
    val exception = assertThrows<IllegalStateException> {
      offeringService.findBuildingChoicesOffering(courseIntensity, courseAudience, organisationId)
    }

    // Then
    assertThat(exception).hasMessage("No courses found for intensity MEDIUM and audience General Offence")
  }

  @Test
  fun `should throw IllegalStateException when multiple matching courses are found`() {
    // Given
    val courseIntensity = "MEDIUM"
    val courseAudience = "General Offence"
    val organisationId = "HIK"
    val course1 = CourseEntityFactory().withAudience(courseAudience).withIntensity(courseIntensity).produce()
    val course2 = CourseEntityFactory().withAudience(courseAudience).withIntensity(courseIntensity).produce()
    val course3 = CourseEntityFactory().withAudience(courseAudience).withIntensity(courseIntensity).produce()

    whenever(courseService.getBuildingChoicesCourses()).thenReturn(listOf(course1, course2, course3))

    //  When
    val exception = assertThrows<IllegalStateException> {
      offeringService.findBuildingChoicesOffering(courseIntensity, courseAudience, organisationId)
    }

    // Then
    assertThat(exception).hasMessage("Multiple courses found for intensity MEDIUM and audience General Offence")
  }

  @Test
  fun `should throw IllegalStateException when no offerings are found for a matched course`() {
    // Given
    val courseIntensity = "MEDIUM"
    val courseAudience = "General Offence"
    val organisationId = "HIK"
    val courseId = UUID.randomUUID()
    val course = CourseEntityFactory().withId(courseId).withAudience(courseAudience).withIntensity(courseIntensity).produce()

    whenever(courseService.getBuildingChoicesCourses()).thenReturn(listOf(course))
    whenever(offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId, organisationId)).thenReturn(null)

    // When
    val exception = assertThrows<IllegalStateException> {
      offeringService.findBuildingChoicesOffering(courseIntensity, courseAudience, organisationId)
    }

    // Then
    assertThat(exception).hasMessage("No active offering found for courseId $courseId and organisationId HIK")
  }

  @Test
  fun `should return offering when a matching offering is found`() {
    // Given
    val courseIntensity = "MEDIUM"
    val courseAudience = "General Offence"
    val organisationId = "HIK"
    val courseId = UUID.randomUUID()
    val course = CourseEntityFactory().withId(courseId).withAudience(courseAudience).withIntensity(courseIntensity).produce()
    val expectedOffering = OfferingEntityFactory().produce()

    whenever(courseService.getBuildingChoicesCourses()).thenReturn(listOf(course))
    whenever(offeringRepository.findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId, organisationId)).thenReturn(expectedOffering)

    // When
    val result = offeringService.findBuildingChoicesOffering(courseIntensity, courseAudience, organisationId)

    // Then
    assertThat(result).isEqualTo(expectedOffering)
  }
}
