package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import java.time.LocalDateTime

@Service
@Transactional
class SubjectAccessRequestService(private val repository: ReferralRepository) {

  fun getPrisonContentFor(prisonerNumber: String, fromDate: LocalDateTime?, toDate: LocalDateTime?) =
    HmppsSubjectAccessRequestContent(repository.getSarReferrals(prisonerNumber, fromDate, toDate).toSarReferral())
}

private fun List<ReferralEntity>.toSarReferral(): List<SarReferral> {
  return map {
    it.id
    SarReferral(
      it.prisonNumber,
      it.oasysConfirmed,
      it.status,
      it.hasReviewedProgrammeHistory,
      it.additionalInformation,
      it.submittedOn,
      it.referrer.username,
      it.offering.course.name,
      it.offering.course.audience,
      it.offering.organisationId,
    )
  }
}

data class HmppsSubjectAccessRequestContent(val content: List<SarReferral>)

data class SarReferral(
  val prisonerNumber: String,
  val oasysConfirmed: Boolean,
  val statusCode: String,
  val hasReviewedProgrammeHistory: Boolean,
  val additionalInformation: String?,
  val submittedOn: LocalDateTime?,
  val referrerUsername: String?,
  val courseName: String,
  val audience: String?,
  val courseOrganisation: String?,
)
