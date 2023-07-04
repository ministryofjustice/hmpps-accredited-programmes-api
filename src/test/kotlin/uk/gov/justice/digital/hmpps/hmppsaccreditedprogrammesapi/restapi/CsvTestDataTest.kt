package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test

class CsvTestDataTest {
  @Test
  fun `identity transform`() {
    (
      "name,description,course,identifier,comments,,,,\n" +
        CsvTestData.prerequisiteRecords.joinToString("\n") { """"${it.name}","${it.description}","${it.course}","${it.identifier}","${it.comments}",,,,""" } +
        "\n"
      ) shouldBeEqual CsvTestData.prerequisitesCsvText
  }
}
