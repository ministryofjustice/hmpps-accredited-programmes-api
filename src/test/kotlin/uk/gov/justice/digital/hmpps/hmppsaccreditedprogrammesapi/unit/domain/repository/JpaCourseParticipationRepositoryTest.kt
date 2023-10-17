package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.TEST_USER_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationOutcome
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaCourseEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaCourseParticipationRepository
import java.time.Duration
import java.time.LocalDateTime
import java.time.Year
import kotlin.jvm.optionals.getOrNull

class JpaCourseParticipationRepositoryTest
@Autowired
constructor(
  val courseParticipationRepository: JpaCourseParticipationRepository,
  val courseEntityRepository: JpaCourseEntityRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {
  @Test
  fun `Should save and retrieve a CourseParticipation entity`() {
    val courseId = courseEntityRepository.save(CourseEntity(name = "A Course", identifier = "ID")).id!!
    val startTime = LocalDateTime.now()
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipationEntity(
        courseId = courseId,
        courseName = "Course name",
        prisonNumber = prisonNumber,
        otherCourseName = null,
        source = "Source of information",
        detail = "Course detail",
        outcome = CourseParticipationOutcome(
          status = CourseStatus.COMPLETE,
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
      CourseParticipationEntity(
        id = participationId,
        courseName = "Course name",
        courseId = courseId,
        prisonNumber = prisonNumber,
        otherCourseName = null,
        source = "Source of information",
        detail = "Course detail",
        outcome = CourseParticipationOutcome(
          status = CourseStatus.COMPLETE,
          yearStarted = Year.parse("2021"),
          yearCompleted = Year.parse("2022"),
        ),
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY, location = "location"),
        createdByUsername = TEST_USER_NAME,
      ),
      CourseParticipationEntity::createdDateTime,
    )
    persistentHistory.createdDateTime.shouldBeWithin(Duration.ofSeconds(1), startTime)
  }

  @Test
  fun `Should save and retrieve a CourseParticipation entity having all nullable fields set to null`() {
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipationEntity(
        courseName = null,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = null,
        outcome = null,
      ),
    ).id!!

    commitAndStartNewTx()

    val persistentHistory = courseParticipationRepository.findById(participationId).getOrNull()

    persistentHistory.shouldNotBeNull()

    persistentHistory.shouldBeEqualToIgnoringFields(
      CourseParticipationEntity(
        id = participationId,
        courseName = null,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = null,
        outcome = null,
        createdByUsername = TEST_USER_NAME,
      ),
      CourseParticipationEntity::createdDateTime,
    )
  }

  @Test
  fun `Should save and update a CouseParticipation entity with all auditable fields`() {
    val startTime = LocalDateTime.now()
    val prisonNumber = randomPrisonNumber()

    val participationId = courseParticipationRepository.save(
      CourseParticipationEntity(
        courseName = null,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.COMMUNITY, location = null),
        outcome = CourseParticipationOutcome(status = CourseStatus.COMPLETE, yearStarted = null, yearCompleted = null),
      ),
    ).id!!

    val persistentHistory = courseParticipationRepository.findById(participationId).get()
    persistentHistory.setting?.type = CourseSetting.CUSTODY

    commitAndStartNewTx()

    persistentHistory.shouldBeEqualToIgnoringFields(
      CourseParticipationEntity(
        id = participationId,
        courseName = null,
        courseId = null,
        prisonNumber = prisonNumber,
        otherCourseName = "Other course name",
        source = null,
        detail = null,
        setting = CourseParticipationSetting(type = CourseSetting.CUSTODY, location = null),
        outcome = CourseParticipationOutcome(status = CourseStatus.COMPLETE, yearStarted = null, yearCompleted = null),
        createdByUsername = TEST_USER_NAME,
        lastModifiedByUsername = TEST_USER_NAME,
      ),
      CourseParticipationEntity::createdDateTime,
      CourseParticipationEntity::lastModifiedDateTime,
    )

    persistentHistory.createdDateTime.shouldBeWithin(Duration.ofSeconds(1), startTime)
    persistentHistory.lastModifiedDateTime.shouldNotBeNull()
    persistentHistory.lastModifiedDateTime!!.shouldBeWithin(Duration.ofSeconds(1), startTime)
  }
}
