package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.SubjectAccessRequestApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import java.time.Instant
import java.time.ZoneId

@RestController
@PreAuthorize("hasAnyRole('ROLE_SAR_DATA_ACCESS', 'ROLE_ACCREDITED_PROGRAMMES_API')")
class SarsDataController(
  private val subjectAccessRequestService: SubjectAccessRequestService,
) : SubjectAccessRequestApiDelegate {

  override fun subjectAccessRequestGet(prn: String?, fromDate: Instant?, toDate: Instant?): ResponseEntity<Any> {
    if (prn == null) {
      return ResponseEntity("PRN is required", HttpStatus.BAD_REQUEST)
    }

    val startDate = fromDate?.atZone(ZoneId.systemDefault())?.toLocalDate()?.atStartOfDay()
    val endDate = toDate?.atZone(ZoneId.systemDefault())?.toLocalDate()?.plusDays(1)?.atStartOfDay()

    val sarsData = subjectAccessRequestService.getPrisonContentFor(prn, startDate, endDate)

    return if (sarsData.referrals.isEmpty() && sarsData.courseParticipation.isEmpty()) {
      ResponseEntity(HttpStatus.NO_CONTENT)
    } else {
      ResponseEntity(sarsData, HttpStatus.OK)
    }
  }
}
