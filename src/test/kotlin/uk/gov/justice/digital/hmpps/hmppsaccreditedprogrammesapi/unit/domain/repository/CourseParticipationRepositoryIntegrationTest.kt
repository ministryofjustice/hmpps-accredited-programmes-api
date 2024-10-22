package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CourseParticipationRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var courseParticipationRepository: CourseParticipationRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createParticipation(UUID.fromString("0cff5da9-1e90-4ee2-a5cb-94dc49c4b004"), "A1234AA", "Green Course", "squirrel", "Some detail", "Schulist End", "COMMUNITY", "INCOMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e"), "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("882a5a16-bcb8-4d8b-9692-a3006dcecffb"), "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
    persistenceHelper.createParticipation(UUID.fromString("cc8eb19e-050a-4aa9-92e0-c654e5cfe281"), "A1234AA", "Orange Course", "squirrel", "This participation will be updated", "Schulist End", "COMMUNITY", "COMPLETE", 2023, null, "Carmelo Conn", LocalDateTime.parse("2023-10-11T13:11:06"), null, null)
  }

  @Test
  fun `should retrieve all completed course participations for prison number`() {
    // Given
    val prisonNumber = "A1234AA"
    val outcomes = listOf(CourseStatus.COMPLETE)

    // When
    val completedCourseParticipations = courseParticipationRepository.findByPrisonNumberAndOutcomeStatusIn(prisonNumber, outcomes)

    // Then
    assertThat(completedCourseParticipations).hasSize(1)
    assertThat(completedCourseParticipations[0].courseName).isEqualTo("Orange Course")
    assertThat(completedCourseParticipations[0].detail).isEqualTo("This participation will be updated")
  }

  @Test
  fun `should retrieve all incomplete course participations for prison number`() {
    // Given
    val prisonNumber = "A1234AA"
    val outcomes = listOf(CourseStatus.INCOMPLETE)

    // When
    val completedCourseParticipations = courseParticipationRepository.findByPrisonNumberAndOutcomeStatusIn(prisonNumber, outcomes)

    // Then
    assertThat(completedCourseParticipations).hasSize(2)
    assertThat(completedCourseParticipations[0].courseName).isEqualTo("Green Course")
    assertThat(completedCourseParticipations[0].detail).isEqualTo("Some detail")
    assertThat(completedCourseParticipations[1].courseName).isEqualTo("Red Course")
    assertThat(completedCourseParticipations[1].detail).isEqualTo("Some detail")
  }

  @Test
  fun `should retrieve all complete and incomplete course participations for prison number`() {
    // Given
    val prisonNumber = "A1234AA"
    val outcomes = listOf(CourseStatus.INCOMPLETE, CourseStatus.COMPLETE)

    // When
    val completedCourseParticipations = courseParticipationRepository.findByPrisonNumberAndOutcomeStatusIn(prisonNumber, outcomes)

    // Then
    assertThat(completedCourseParticipations).hasSize(3)
    assertThat(completedCourseParticipations.count { it.outcome?.status == CourseStatus.COMPLETE }).isEqualTo(1)
    assertThat(completedCourseParticipations.count { it.outcome?.status == CourseStatus.INCOMPLETE }).isEqualTo(2)
  }
}
