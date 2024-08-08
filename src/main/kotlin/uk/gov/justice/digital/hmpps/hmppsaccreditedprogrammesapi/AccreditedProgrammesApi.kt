package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AccreditedProgrammesApi

fun main(args: Array<String>) {
  runApplication<AccreditedProgrammesApi>(*args)
}
