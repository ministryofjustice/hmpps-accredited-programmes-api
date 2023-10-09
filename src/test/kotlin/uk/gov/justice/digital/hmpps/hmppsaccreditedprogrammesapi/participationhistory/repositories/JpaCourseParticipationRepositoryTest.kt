package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories

import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.jpa.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.TEST_USER_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories.CourseEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseStatus
import java.time.Duration
import java.time.LocalDateTime
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
  fun `It should successfully save and retrieve a CourseParticipation entity`() {
    val courseId = courseEntityRepository.save(CourseEntity(name = "A Course", identifier = "ID")).id!!
    val startTime = LocalDateTime.now()
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipation(
        courseId = courseId,
        prisonNumber = prisonNumber,
        otherCourseName = null,
        source = "source",
        detail = "Course detail",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Course outcome detail",
          yearStarted = Year.parse("2021"),
          yearCompleted = Year.parse("2022"),
        ),
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY, location = "location"),
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentHistory = courseParticipationRepository.findById(participationId).getOrNull()

    persistentHistory.shouldNotBeNull()

    persistentHistory.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = participationId,
        courseId = courseId,
        prisonNumber = prisonNumber,
        otherCourseName = null,
        source = "source",
        detail = "Course detail",
        outcome = CourseOutcome(
          status = CourseStatus.COMPLETE,
          detail = "Course outcome detail",
          yearStarted = Year.parse("2021"),
          yearCompleted = Year.parse("2022"),
        ),
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY, location = "location"),
        createdByUsername = TEST_USER_NAME,
      ),
      CourseParticipation::createdDateTime,
    )
    persistentHistory.createdDateTime.shouldBeWithin(Duration.ofSeconds(1), startTime)
  }

  @Test
  fun `It should successfully save and retrieve a CourseParticipation entity having all nullable fields set to null`() {
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipation(
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
        outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentHistory = courseParticipationRepository.findById(participationId).getOrNull()

    persistentHistory.shouldNotBeNull()

    persistentHistory.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = participationId,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
        outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
        createdByUsername = TEST_USER_NAME,
      ),
      CourseParticipation::createdDateTime,
    )
  }

  @Test
  fun `save and update a course participation history - audit fields`() {
    val startTime = LocalDateTime.now()
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipation(
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
        outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
      ),
    ).id!!

    val persistentHistory = courseParticipationRepository.findById(participationId).get()
    persistentHistory.setting.type = CourseSetting.CUSTODY

    commitAndStartNewTx()

    persistentHistory.shouldBeEqualToIgnoringFields(
      CourseParticipation(
        id = participationId,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY, location = null),
        outcome = CourseOutcome(status = null, detail = null, yearStarted = null, yearCompleted = null),
        createdByUsername = TEST_USER_NAME,
        lastModifiedByUsername = TEST_USER_NAME,
      ),
      CourseParticipation::createdDateTime,
      CourseParticipation::lastModifiedDateTime,
    )

    persistentHistory.createdDateTime.shouldBeWithin(Duration.ofSeconds(1), startTime)
    persistentHistory.lastModifiedDateTime.shouldNotBeNull()
    persistentHistory.lastModifiedDateTime!!.shouldBeWithin(Duration.ofSeconds(1), startTime)
  }
}
