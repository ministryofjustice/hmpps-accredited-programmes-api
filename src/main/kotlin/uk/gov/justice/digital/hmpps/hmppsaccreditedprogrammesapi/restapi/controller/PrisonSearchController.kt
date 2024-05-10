package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.PrisonSearchApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toPrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService

@Service
class PrisonSearchController
@Autowired
constructor(
  private val prisonRegisterApiService: PrisonRegisterApiService,
) : PrisonSearchApiDelegate {

  override fun getPrisons(prisonSearchRequest: PrisonSearchRequest): ResponseEntity<List<PrisonSearchResponse>> {
    return ResponseEntity.ok(
      prisonRegisterApiService.getPrisons(prisonSearchRequest.prisonIds)
        .map { it.toPrisonSearchResponse() },
    )
  }

  override fun getPrisonById(prisonId: String): ResponseEntity<PrisonSearchResponse> {
    return prisonRegisterApiService.getPrisonById(prisonId)?.let {
      ResponseEntity.ok(it.toPrisonSearchResponse())
    } ?: throw NotFoundException("No Prison found for $prisonId")
  }
}
