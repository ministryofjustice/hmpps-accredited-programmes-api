package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.HmppsSubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.SubjectAccessRequestService
import java.time.LocalDate

@RestController
class SarsDataController(
  private val subjectAccessRequestService: SubjectAccessRequestService,
) {

  @GetMapping("/subject-access-request")
  fun sarData(
    @RequestParam prisonerNumber: String?,
    @RequestParam fromDate: LocalDate? = null,
    @RequestParam toDate: LocalDate? = null,
    authentication: JwtAuthenticationToken,
  ): ResponseEntity<HmppsSubjectAccessRequestContent> {
    if (prisonerNumber == null) {
      return ResponseEntity(null, null, 209)
    }
    val sarsData =
      subjectAccessRequestService.getPrisonContentFor(prisonerNumber, fromDate?.atStartOfDay(), toDate?.atStartOfDay())
    if (sarsData.content.isEmpty()) {
      return ResponseEntity(sarsData, HttpStatus.NO_CONTENT)
    }
    return ResponseEntity<HmppsSubjectAccessRequestContent>(sarsData, HttpStatus.OK)
  }
}
