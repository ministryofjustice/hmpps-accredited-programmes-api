package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.jparepo

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo.CourseEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import kotlin.jvm.optionals.getOrNull

class JpaCourseParticipationHistoryRepositoryTest
@Autowired
constructor(
  val courseParticipationHistoryRepository: JpaCourseParticipationHistoryRepository,
  val courseEntityRepository: CourseEntityRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `save and retrieve a course participation history`() {
    val courseId = courseEntityRepository.save(CourseEntity(name = "A Course", identifier = "ID")).id!!

    val participationId = courseParticipationHistoryRepository.save(
      CourseParticipationHistory(
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

    persistentHistory shouldBeEqualToComparingFields CourseParticipationHistory(
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
  fun `save and retrieve a course participation history with minimal fields`() {
    val participationId = courseParticipationHistoryRepository.save(
      CourseParticipationHistory(
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

    persistentHistory shouldBeEqualToComparingFields CourseParticipationHistory(
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
