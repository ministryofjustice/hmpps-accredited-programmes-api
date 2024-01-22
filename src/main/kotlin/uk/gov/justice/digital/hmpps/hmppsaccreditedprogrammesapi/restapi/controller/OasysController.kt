package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.OasysApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
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

  override fun getLifestyle(prisonNumber: String): ResponseEntity<Lifestyle> =
    ResponseEntity
      .ok(
        oasysService
          .getLifestyle(prisonNumber),
      )

  override fun getRoshAnalysis(prisonNumber: String): ResponseEntity<RoshAnalysis> =
    ResponseEntity
      .ok(
        oasysService
          .getRoshFull(prisonNumber),
      )

  override fun getPsychiatric(prisonNumber: String): ResponseEntity<Psychiatric> =
    ResponseEntity
      .ok(
        oasysService
          .getPsychiatric(prisonNumber),
      )

  override fun getBehaviour(prisonNumber: String): ResponseEntity<Behaviour> =
    ResponseEntity
      .ok(
        oasysService
          .getBehaviour(prisonNumber),
      )

  override fun getHealth(prisonNumber: String): ResponseEntity<Health> =
    ResponseEntity
      .ok(
        oasysService
          .getHealth(prisonNumber),
      )

  override fun getAttitude(prisonNumber: String): ResponseEntity<Attitude> =
    ResponseEntity
      .ok(
        oasysService
          .getAttitude(prisonNumber),
      )

  override fun getLearningNeeds(prisonNumber: String): ResponseEntity<LearningNeeds> =
    ResponseEntity
      .ok(
        oasysService
          .getLearningNeeds(prisonNumber),
      )
}
