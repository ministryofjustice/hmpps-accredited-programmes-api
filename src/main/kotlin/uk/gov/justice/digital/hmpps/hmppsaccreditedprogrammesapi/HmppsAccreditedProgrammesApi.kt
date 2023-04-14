package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class HmppsAccreditedProgrammesApi

fun main(args: Array<String>) {
  runApplication<HmppsAccreditedProgrammesApi>(*args)
}
