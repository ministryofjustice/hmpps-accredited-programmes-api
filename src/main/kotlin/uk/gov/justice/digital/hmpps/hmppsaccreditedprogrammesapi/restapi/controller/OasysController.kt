package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OasysApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.OasysService

@Service
class OasysController(val oasysService: OasysService) : OasysApiDelegate {
  override fun getOffenceDetails(prisonNumber: String): ResponseEntity<OffenceDetail> =
    ResponseEntity
      .ok(
        oasysService
          .getOffenceDetail(prisonNumber),
      )

  override fun getRelationships(prisonNumber: String): ResponseEntity<Relationships> =
    ResponseEntity
      .ok(
        oasysService
          .getRelationships(prisonNumber),
      )
}