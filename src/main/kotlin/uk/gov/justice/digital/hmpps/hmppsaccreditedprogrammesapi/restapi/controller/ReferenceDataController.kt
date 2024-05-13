package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ReferenceDataApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService

@Service
class ReferenceDataController
@Autowired
constructor(
  private val referenceDataService: ReferralReferenceDataService,
) : ReferenceDataApiDelegate {
  override fun getReferralStatuses(): ResponseEntity<List<ReferralStatusRefData>> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatuses(),
    )

  override fun getReferralStatus(code: String): ResponseEntity<ReferralStatusRefData> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatus(code),
    )

  override fun getReferralStatusCategories(referralStatusCode: String): ResponseEntity<List<ReferralStatusCategory>> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatusCategories(referralStatusCode),
    )

  override fun getReferralStatusCategory(code: String): ResponseEntity<ReferralStatusCategory> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatusCategory(code),
    )

  override fun getReferralStatusReasons(referralStatusCode: String, categoryCode: String, deselectAndKeepOpen: Boolean): ResponseEntity<List<ReferralStatusReason>> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatusReasons(referralStatusCode, categoryCode, deselectAndKeepOpen),
    )

  override fun getReferralStatusReason(code: String): ResponseEntity<ReferralStatusReason> =
    ResponseEntity.ok(
      referenceDataService
        .getReferralStatusReason(code),
    )
}
