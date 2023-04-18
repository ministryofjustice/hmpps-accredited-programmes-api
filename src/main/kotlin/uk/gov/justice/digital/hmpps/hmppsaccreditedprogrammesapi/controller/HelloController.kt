package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.HelloApiDelegate

@Service
class HelloController : HelloApiDelegate {
  override fun helloGet(): ResponseEntity<String> {
    return ResponseEntity.ok("Welcome to the Accredited Programs API")
  }
}