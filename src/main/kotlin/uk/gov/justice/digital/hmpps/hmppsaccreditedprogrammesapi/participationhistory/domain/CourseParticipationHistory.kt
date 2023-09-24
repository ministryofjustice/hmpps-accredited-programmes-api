package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.Hibernate
import org.hibernate.annotations.Formula
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shareddomain.BusinessException
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID

@Entity
@Table(name = "course_participation")
@EntityListeners(AuditingEntityListener::class)
class CourseParticipationHistory(
  @Id
  @GeneratedValue
  @Column(name = "course_participation_id")
  val id: UUID? = null,

  val prisonNumber: String,
  var courseId: UUID? = null,
  var otherCourseName: String?,
  var source: String?,

  @Embedded
  var setting: CourseParticipationSetting,

  @Embedded
  val outcome: CourseOutcome,

  @CreatedBy
  var createdByUsername: String = "anonymous",

  @CreatedDate
  var createdDateTime: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedBy
  var lastModifiedByUsername: String? = null,

  @LastModifiedDate
  var lastModifiedDateTime: LocalDateTime? = null,
) {
  fun assertOnlyCourseIdOrCourseNamePresent() {
    if (courseId == null && otherCourseName == null) {
      throw BusinessException("Expected a courseId or otherCourseName but neither value is present")
    }
    if (courseId != null && otherCourseName != null) {
      throw BusinessException("Expected just one of courseId or otherCourseName but both values are present")
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as CourseParticipationHistory
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1004284837
}

@Embeddable
data class CourseParticipationSetting(
  var location: String?,

  @Enumerated(EnumType.STRING)
  var type: CourseSetting,
)

@Embeddable
data class CourseOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  var status: CourseStatus?,

  @Column(name = "outcome_detail")
  var detail: String?,

  var yearStarted: Year?,
  var yearCompleted: Year?,

  @Formula("0")
  private val ignoreMe: Int = 0, // This unused, non-nullable field forces Hibernate to create an @Embedded instance when all fields are null.
)

enum class CourseSetting { CUSTODY, COMMUNITY }
enum class CourseStatus { INCOMPLETE, COMPLETE }
