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
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@RestController
class SarsDataController(
  private val subjectAccessRequestService: SubjectAccessRequestService,
) {

  @GetMapping("/subject-access-request")
  fun sarData(
    @RequestParam prisonerNumber: String?,
    @RequestParam fromDate: String? = null,
    @RequestParam toDate: String? = null,
    authentication: JwtAuthenticationToken,
  ): ResponseEntity<HmppsSubjectAccessRequestContent> {
    if (prisonerNumber == null) {
      return ResponseEntity(null, null, 209)
    }

    val offsetFromDate = fromDate?.let {
      OffsetDateTime.of(
          LocalDate.parse(fromDate, DateTimeFormatter.ISO_DATE),
          LocalTime.MIDNIGHT,
          ZoneOffset.UTC,
      )
    }
    val offsetToDate = toDate?.let {
      OffsetDateTime.of(
          LocalDate.parse(toDate, DateTimeFormatter.ISO_DATE),
          LocalTime.MIDNIGHT,
          ZoneOffset.UTC,
      )
    }

    val sarsData =
      subjectAccessRequestService.getPrisonContentFor(prisonerNumber, offsetFromDate, offsetToDate)
    if (sarsData.referrals.isEmpty() && sarsData.courseParticipation.isEmpty()) {
      return ResponseEntity(sarsData, HttpStatus.NO_CONTENT)
    }
    return ResponseEntity<HmppsSubjectAccessRequestContent>(sarsData, HttpStatus.OK)
  }
}
