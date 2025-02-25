package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.util.ResourceUtils

object ResourceLoader {
  val MAPPER: ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  inline fun <reified T> file(filename: String): T = MAPPER.readValue(
    ResourceUtils.getFile("classpath:simulations/__files/$filename.json"),
  )
}
