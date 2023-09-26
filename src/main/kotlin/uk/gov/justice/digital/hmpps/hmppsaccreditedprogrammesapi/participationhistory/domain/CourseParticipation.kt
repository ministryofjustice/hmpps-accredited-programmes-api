package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.domain.BusinessException
import java.time.Year
import java.util.UUID

@Entity
@Table(name = "course_participation_history")
class CourseParticipation(
  @Id
  @GeneratedValue
  @Column(name = "course_participation_history_id")
  val id: UUID? = null,

  val prisonNumber: String,
  var courseId: UUID? = null,
  var otherCourseName: String?,
  var yearStarted: Year?,
  var source: String?,

  @Enumerated(EnumType.STRING)
  var setting: CourseSetting?,

  @Embedded
  var outcome: CourseOutcome?,
) {
  fun assertOnlyCourseIdOrCourseNamePresent() {
    if (courseId == null && otherCourseName == null) {
      throw BusinessException("Expected a courseId or otherCourseName but neither value is present")
    }
    if (courseId != null && otherCourseName != null) {
      throw BusinessException("Expected just one of courseId or otherCourseName but both values are present")
    }
  }
}

@Embeddable
class CourseOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  var status: CourseStatus?,

  @Column(name = "outcome_detail")
  var detail: String?,
)

enum class CourseSetting {
  CUSTODY,
  COMMUNITY,
  ;

  override fun toString(): String = name.lowercase()
}

enum class CourseStatus {
  DESELECTED,
  INCOMPLETE,
  COMPLETE,
  ;

  override fun toString(): String = name.lowercase()
}
