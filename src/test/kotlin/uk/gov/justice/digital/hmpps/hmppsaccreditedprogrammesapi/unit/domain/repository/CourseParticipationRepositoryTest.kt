package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationSettingFactory
import java.time.Duration
import java.time.LocalDateTime
import java.time.Year

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test-h2")
class CourseParticipationRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Test
  fun `CourseParticipationRepository should save and retrieve CourseParticipationEntity objects`() {
    val transactionStartTime = LocalDateTime.now()

    val courseParticipationSetting = CourseParticipationSettingFactory()
      .withType(CourseSetting.CUSTODY)
      .withLocation("Location")
      .produce()
    val courseParticipationOutcome = CourseParticipationOutcomeFactory()
      .withStatus(CourseStatus.COMPLETE)
      .withYearStarted(Year.parse("2021"))
      .withYearCompleted(Year.parse("2022"))
      .produce()
    var courseParticipation = CourseParticipationEntityFactory()
      .withId(null)
      .withCourseName("Course name")
      .withPrisonNumber(PRISON_NUMBER_1)
      .withSource("Source of information")
      .withDetail("Course detail")
      .withSetting(courseParticipationSetting)
      .withOutcome(courseParticipationOutcome)
      .withCreatedByUsername(REFERRER_USERNAME)
      .produce()
    courseParticipation = entityManager.merge(courseParticipation)

    val persistedCourseParticipation = entityManager.find(CourseParticipationEntity::class.java, courseParticipation.id)

    persistedCourseParticipation shouldNotBe null
    persistedCourseParticipation.run {
      this.prisonNumber shouldBe PRISON_NUMBER_1
      this.setting?.shouldBeEqualToIgnoringFields(courseParticipationSetting, CourseParticipationEntity::id)
      this.outcome?.shouldBeEqualToIgnoringFields(courseParticipationOutcome, CourseParticipationEntity::id)
      this.createdByUsername shouldBe REFERRER_USERNAME
    }
    persistedCourseParticipation?.createdDateTime?.shouldBeWithin(Duration.ofSeconds(1), transactionStartTime)
  }

  @Test
  fun `CourseParticipationRepository should save and retrieve CourseParticipationEntity objects, having all nullable fields set to null`() {
    var courseParticipation = CourseParticipationEntityFactory()
      .withId(null)
      .withCourseName(null)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withSource(null)
      .withDetail(null)
      .withSetting(null)
      .withOutcome(null)
      .withCreatedByUsername(REFERRER_USERNAME)
      .produce()
    courseParticipation = entityManager.merge(courseParticipation)

    val persistedCourseParticipation = entityManager.find(CourseParticipationEntity::class.java, courseParticipation.id)
    persistedCourseParticipation shouldNotBe null
    persistedCourseParticipation.run {
      this.courseName shouldBe null
      this.prisonNumber shouldBe PRISON_NUMBER_1
      this.source shouldBe null
      this.detail shouldBe null
      this.setting shouldBe null
      this.outcome shouldBe null
      this.createdByUsername shouldBe REFERRER_USERNAME
    }
  }

  @Test
  fun `CourseParticipationRepository should save and update CourseParticipationEntity objects, having all auditable fields set`() {
    val transactionStartTime = LocalDateTime.now()

    val courseParticipationSetting = CourseParticipationSettingFactory()
      .withType(CourseSetting.CUSTODY)
      .withLocation("Location")
      .produce()
    val courseParticipationOutcome = CourseParticipationOutcomeFactory()
      .withStatus(CourseStatus.COMPLETE)
      .withYearStarted(Year.parse("2021"))
      .withYearCompleted(Year.parse("2022"))
      .produce()
    var courseParticipation = CourseParticipationEntityFactory()
      .withId(null)
      .withCourseName(null)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withSource(null)
      .withDetail(null)
      .withSetting(courseParticipationSetting)
      .withOutcome(courseParticipationOutcome)
      .withCreatedByUsername(REFERRER_USERNAME)
      .produce()
    courseParticipation = entityManager.merge(courseParticipation)

    val persistedCourseParticipation = entityManager.find(CourseParticipationEntity::class.java, courseParticipation.id)
    persistedCourseParticipation shouldNotBe null
    persistedCourseParticipation.run {
      this.courseName shouldBe null
      this.prisonNumber shouldBe PRISON_NUMBER_1
      this.source shouldBe null
      this.detail shouldBe null
      this.setting?.shouldBeEqualToIgnoringFields(courseParticipationSetting, CourseParticipationEntity::id)
      this.outcome?.shouldBeEqualToIgnoringFields(courseParticipationOutcome, CourseParticipationEntity::id)
      this.createdByUsername shouldBe REFERRER_USERNAME
      this.lastModifiedByUsername shouldBe null
    }
    persistedCourseParticipation.createdDateTime.shouldBeWithin(Duration.ofSeconds(2), transactionStartTime)
  }
}
