package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PrisonerSearchApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonerSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonerSearchApiService

@Service
class PrisonSearchController
@Autowired
constructor(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val auditService: AuditService,
) : PrisonerSearchApiDelegate {
  override fun searchPrisoner(prisonerSearchRequest: PrisonerSearchRequest): ResponseEntity<List<PrisonerSearchResponse>> {
    auditService.audit(
      prisonNumber = prisonerSearchRequest.prisonerIdentifier,
      auditAction = AuditAction.SEARCH_FOR_PERSON.name,
    )

    return ResponseEntity.ok(
      prisonerSearchApiService.searchPrisoners(prisonerSearchRequest)
        .map {
          PrisonerSearchResponse(
            bookingId = it.bookingId,
            conditionalReleaseDate = it.conditionalReleaseDate,
            prisonName = it.prisonName,
            dateOfBirth = it.dateOfBirth,
            ethnicity = it.ethnicity,
            gender = it.gender,
            homeDetentionCurfewEligibilityDate = it.homeDetentionCurfewEligibilityDate,
            indeterminateSentence = it.indeterminateSentence,
            firstName = it.firstName,
            lastName = it.lastName,
            paroleEligibilityDate = it.paroleEligibilityDate,
            prisonerNumber = it.prisonerNumber,
            religion = it.religion,
            sentenceExpiryDate = it.sentenceExpiryDate,
            sentenceStartDate = it.sentenceStartDate,
            tariffDate = it.tariffDate,
          )
        },
    )
  }
}
