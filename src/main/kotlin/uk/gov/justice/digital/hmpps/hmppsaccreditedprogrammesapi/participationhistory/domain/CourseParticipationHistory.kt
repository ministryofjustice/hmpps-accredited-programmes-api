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
  var courseId: UUID? = null,
  var otherCourseName: String?,

  var yearStarted: Year?,

  @Enumerated(EnumType.STRING)
  var setting: CourseSetting?,

  @Embedded
  var outcome: CourseOutcome?,
)

@Embeddable
class CourseOutcome(
  @Enumerated(EnumType.STRING)
  @Column(name = "outcome_status")
  var status: CourseStatus?,

  @Column(name = "outcome_detail")
  var detail: String?,
)

enum class CourseSetting { CUSTODY, COMMUNITY }
enum class CourseStatus { DESELECTED, INCOMPLETE, COMPLETE }
