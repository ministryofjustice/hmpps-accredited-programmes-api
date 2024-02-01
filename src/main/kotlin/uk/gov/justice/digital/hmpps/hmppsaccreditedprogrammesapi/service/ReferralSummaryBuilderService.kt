package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Name
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Sentence
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary as ApiReferralSummary

@Service
class ReferralSummaryBuilderService {

  fun build(
    content: List<ReferralSummaryProjection>,
    prisoners: List<Prisoner>,
    prisons: Map<String?, String?>,
    includeEarliestReleaseDate: Boolean,
  ): List<ApiReferralSummary> {
    return content.groupBy { it.referralId }
      .map { (id, projections) ->
        val firstProjection = projections.first()
        val sentence = getSentence(firstProjection, prisoners)

        ApiReferralSummary(
          id = id,
          referrerUsername = firstProjection.referrerUsername,
          courseName = firstProjection.courseName,
          audience = firstProjection.audience,
          status = firstProjection.status.toApi(),
          submittedOn = firstProjection.submittedOn?.toString(),
          prisonNumber = firstProjection.prisonNumber,
          organisationId = firstProjection.organisationId,
          prisonName = prisons[firstProjection.organisationId].orEmpty(),
          prisonerName = getPrisonerName(firstProjection, prisoners),
          sentence = sentence,
          earliestReleaseDate = if (includeEarliestReleaseDate) getEarliestReleaseDate(sentence) else null,
        )
      }
  }

  private fun getSentence(referralSummaryProjection: ReferralSummaryProjection, prisoners: List<Prisoner>): Sentence? {
    return prisoners.find { it.prisonerNumber == referralSummaryProjection.prisonNumber }?.let {
      Sentence(
        conditionalReleaseDate = it.conditionalReleaseDate,
        tariffExpiryDate = it.tariffDate,
        paroleEligibilityDate = it.paroleEligibilityDate,
        indeterminateSentence = it.indeterminateSentence,
        nonDtoReleaseDateType = it.nonDtoReleaseDateType,
      )
    }
  }

  fun getEarliestReleaseDate(sentence: Sentence?): LocalDate? {
    return when {
      sentence?.indeterminateSentence == true -> sentence.tariffExpiryDate
      sentence?.paroleEligibilityDate != null -> sentence.paroleEligibilityDate
      sentence?.conditionalReleaseDate != null -> sentence.conditionalReleaseDate
      else -> null
    }
  }

  private fun getPrisonerName(referralSummaryProjection: ReferralSummaryProjection, prisoners: List<Prisoner>): Name? {
    return prisoners.find { it.prisonerNumber == referralSummaryProjection.prisonNumber }?.let {
      Name(
        firstName = it.firstName,
        lastName = it.lastName,
      )
    }
  }
}
