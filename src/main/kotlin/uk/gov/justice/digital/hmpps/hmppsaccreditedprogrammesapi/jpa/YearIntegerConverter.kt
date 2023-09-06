package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jpa

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.Year

@Converter
class YearIntegerConverter : AttributeConverter<Year, Int> {
  override fun convertToDatabaseColumn(attribute: Year?): Int = attribute?.value
    ?: throw IllegalArgumentException("Year attribute should never be null")

  override fun convertToEntityAttribute(dbData: Int?): Year = dbData?.let(Year::of)
    ?: throw IllegalArgumentException("database value should never be null for conversion to Year")
}
