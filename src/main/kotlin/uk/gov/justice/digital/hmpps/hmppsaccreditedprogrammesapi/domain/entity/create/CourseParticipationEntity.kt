package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

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
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.time.Year
import java.util.UUID

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "course_participation")
data class CourseParticipationEntity(
  @Id
  @GeneratedValue
  @Column(name = "course_participation_id")
  val id: UUID? = null,

  var referralId: UUID? = null,

  var courseId: UUID? = null,

  val prisonNumber: String,
  var courseName: String?,

  var source: String?,
  var detail: String?,

  @Column("is_draft")
  var isDraft: Boolean? = false,

  @Embedded
  var setting: CourseParticipationSetting? = null,

  @Embedded
  var outcome: CourseParticipationOutcome? = null,

  @CreatedBy
  var createdByUsername: String = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @CreatedDate
  var createdDateTime: LocalDateTime = LocalDateTime.MIN,

  @LastModifiedBy
  var lastModifiedByUsername: String? = null,

  @LastModifiedDate
  var lastModifiedDateTime: LocalDateTime? = null,
)

@Embeddable
data class CourseParticipationSetting(
  var location: String? = null,

  @Enumerated(EnumType.STRING)
  var type: CourseSetting,
)

@Embeddable
data class CourseParticipationOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  var status: CourseStatus,

  var yearStarted: Year? = null,
  var yearCompleted: Year? = null,
)

enum class CourseSetting {
  CUSTODY,
  COMMUNITY,
  ;

  override fun toString(): String {
    return this.name.lowercase()
  }
}
enum class CourseStatus {
  INCOMPLETE,
  COMPLETE,
  ;

  override fun toString(): String {
    return this.name.lowercase()
  }
}
