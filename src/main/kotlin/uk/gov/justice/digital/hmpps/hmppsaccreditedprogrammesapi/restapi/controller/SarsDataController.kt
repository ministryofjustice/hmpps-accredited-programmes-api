package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.SubjectAccessRequestApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import java.time.LocalDate

@RestController
@PreAuthorize("hasAnyRole('ROLE_SAR_DATA_ACCESS', 'ROLE_ACCREDITED_PROGRAMMES_API')")
class SarsDataController(
  private val subjectAccessRequestService: SubjectAccessRequestService,
) : SubjectAccessRequestApiDelegate {

  override fun subjectAccessRequestGet(prn: String?, fromDate: LocalDate?, toDate: LocalDate?): ResponseEntity<Any> {
    if (prn == null) {
      return ResponseEntity(null, null, 209)
    }

    val sarsData = subjectAccessRequestService.getPrisonContentFor(prn, fromDate, toDate)

    return if (sarsData.content.referrals.isEmpty() && sarsData.content.courseParticipation.isEmpty()) {
      ResponseEntity(HttpStatus.NO_CONTENT)
    } else {
      ResponseEntity(sarsData, HttpStatus.OK)
    }
  }
}
