package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import java.time.Year
import java.util.UUID

@Entity
class CourseParticipationHistory(
  @Id
  @GeneratedValue
  @Column(name = "course_participation_history_id")
  val id: UUID? = null,

  val prisonNumber: String,
  val courseId: UUID? = null,
  val otherCourseName: String?,

  val yearStarted: Year?,

  @Enumerated(EnumType.STRING)
  val setting: CourseSetting,

  @Embedded
  val outcome: CourseOutcome,
)

@Embeddable
class CourseOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  val status: CourseStatus?,

  @Column(name = "outcome_detail")
  val detail: String?,
)

enum class CourseSetting { CUSTODY, COMMUNITY }
enum class CourseStatus { DESELECTED, INCOMPLETE, COMPLETE }
