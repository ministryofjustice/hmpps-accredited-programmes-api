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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Year
import kotlin.jvm.optionals.getOrNull

class JpaCourseParticipationRepositoryTest
@Autowired
constructor(
  val courseParticipationRepository: JpaCourseParticipationRepository,
  val courseEntityRepository: CourseEntityRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `It should successfully save and retrieve records`() {
    val courseId = courseEntityRepository.save(CourseEntity(name = "A Course", identifier = "ID")).id!!

    val participationId = courseParticipationRepository.save(
      CourseParticipation(
        courseId = courseId,
        prisonNumber = "A1234AA",
        otherCourseName = "Other course name",
        yearStarted = Year.parse("2021"),
        source = "source",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Course outcome detail",
        ),
        setting = CourseSetting.CUSTODY,
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentCourseParticipationi = courseParticipationRepository.findById(participationId).getOrNull()

    persistentCourseParticipationi.shouldNotBeNull()

    persistentCourseParticipationi shouldBeEqualToComparingFields CourseParticipation(
      id = participationId,
      courseId = courseId,
      prisonNumber = "A1234AA",
      otherCourseName = "Other course name",
      yearStarted = Year.parse("2021"),
      source = "source",
      outcome = CourseOutcome(
        status = CourseStatus.COMPLETE,
        detail = "Course outcome detail",
      ),
      setting = CourseSetting.CUSTODY,
    )
  }
}
