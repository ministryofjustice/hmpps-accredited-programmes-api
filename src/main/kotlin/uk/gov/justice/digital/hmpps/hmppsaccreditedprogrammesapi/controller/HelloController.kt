package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.HelloApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Hello

@Service
class HelloController : HelloApiDelegate {
  override fun helloGet(): ResponseEntity<Hello> {
    return ResponseEntity.ok(Hello(message = "Welcome to the Accredited Programs API"))
  }
}
