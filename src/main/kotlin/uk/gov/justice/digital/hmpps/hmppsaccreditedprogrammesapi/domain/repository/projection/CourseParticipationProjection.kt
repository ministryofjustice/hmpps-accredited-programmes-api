package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.projection

import java.util.UUID

interface CourseParticipationProjection {
  fun getPrisonNumber(): String
  fun getId(): UUID
  fun getReferralId(): UUID?
  fun getReferralStatus(): String?
  fun getAddedBy(): String
  fun getCreatedAt(): String
  fun getCourseName(): String?
  fun getType(): String?
  fun getYearStarted(): Int?
  fun getYearCompleted(): Int?
  fun getLocation(): String?
  fun getOutcomeStatus(): String?
  fun getDetail(): String?
  fun getSource(): String?
  fun getIsDraft(): Boolean?
}
