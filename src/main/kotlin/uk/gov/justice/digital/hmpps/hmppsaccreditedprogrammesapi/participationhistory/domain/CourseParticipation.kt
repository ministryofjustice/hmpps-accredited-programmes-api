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
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.domain.BusinessException
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
class CourseParticipation(
  @Id
  @GeneratedValue
  @Column(name = "course_participation_id")
  val id: UUID? = null,

  val prisonNumber: String,
  var courseName: String?,

  @Deprecated("please use CourseParticipation.courseName instead")
  var courseId: UUID? = null,

  @Deprecated("please use CourseParticipation.courseName instead")
  var otherCourseName: String?,

  var source: String?,
  var detail: String?,

  @Embedded
  var setting: CourseParticipationSetting? = null,

  @Embedded
  var outcome: CourseOutcome? = null,

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
    other as CourseParticipation
    return id != null && id == other.id
  }

  override fun hashCode(): Int = 1004284837
}

@Embeddable
data class CourseParticipationSetting(
  var location: String? = null,

  @Enumerated(EnumType.STRING)
  var type: CourseSetting,
)

@Embeddable
data class CourseOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  var status: CourseStatus,

  var yearStarted: Year? = null,
  var yearCompleted: Year? = null,
)

enum class CourseSetting { CUSTODY, COMMUNITY }
enum class CourseStatus { INCOMPLETE, COMPLETE }
