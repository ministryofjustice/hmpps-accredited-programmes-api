package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories.CourseEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import kotlin.jvm.optionals.getOrNull

class JpaCourseParticipationRepositoryTest
@Autowired
constructor(
  val courseParticipationHistoryRepository: JpaCourseParticipationRepository,
  val courseEntityRepository: CourseEntityRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `It should successfully save and retrieve a CourseParticipation entity`() {
    val courseId = courseEntityRepository.save(CourseEntity(name = "A Course", identifier = "ID")).id!!

    val participationId = courseParticipationHistoryRepository.save(
      CourseParticipation(
        courseId = courseId,
        prisonNumber = "A1234AA",
        otherCourseName = null,
        source = "source",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Course outcome detail",
          yearStarted = Year.parse("2021"),
          yearCompleted = Year.parse("2022"),
        ),
        setting = CourseParticipationSetting(
          type = CourseSetting.CUSTODY,
          location = "location",
        ),
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentHistory = courseParticipationHistoryRepository.findById(participationId).getOrNull()

    persistentHistory.shouldNotBeNull()

    persistentHistory shouldBeEqualToComparingFields CourseParticipation(
      id = participationId,
      courseId = courseId,
      prisonNumber = "A1234AA",
      otherCourseName = null,
      source = "source",
      outcome = CourseOutcome(
        status = CourseStatus.COMPLETE,
        detail = "Course outcome detail",
        yearStarted = Year.parse("2021"),
        yearCompleted = Year.parse("2022"),
      ),
      setting = CourseParticipationSetting(
        type = CourseSetting.CUSTODY,
        location = "location",
      ),
    )
  }

  @Test
  fun `It should successfully save and retrieve a CourseParticipation entity having all nullable fields set to null`() {
    val participationId = courseParticipationHistoryRepository.save(
      CourseParticipation(
        courseId = null,
        prisonNumber = "A1234AA",
        otherCourseName = "Other course name",
        source = null,
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
        outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentHistory = courseParticipationHistoryRepository.findById(participationId).getOrNull()

    persistentHistory.shouldNotBeNull()

    persistentHistory shouldBeEqualToComparingFields CourseParticipation(
      id = participationId,
      courseId = null,
      prisonNumber = "A1234AA",
      otherCourseName = "Other course name",
      source = null,
      setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
      outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
    )
  }
}
