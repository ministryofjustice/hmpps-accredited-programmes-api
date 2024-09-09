package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import java.time.LocalDate
import java.util.UUID

class PersonEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var forename: String = randomAlphanumericString()
  private var surname: String = randomAlphanumericString()
  private var prisonNumber: String = randomPrisonNumber()
  private var conditionalReleaseDate: LocalDate = LocalDate.now()
  private var paroleEligibilityDate: LocalDate = LocalDate.now()
  private var tariffExpiryDate: LocalDate = LocalDate.now()
  private var earliestReleaseDateType: String = randomAlphanumericString()
  private var earliestReleaseDate: LocalDate = LocalDate.now()
  private var indeterminateSentence: Boolean = false
  private var nonDtoReleaseDateType: String = randomAlphanumericString()
  private var sentenceType: String = randomAlphanumericString()
  private var location: String = randomAlphanumericString()
  private var gender: String = "Male"

  fun produce() = PersonEntity(
    id = this.id,
    forename = this.forename,
    surname = this.surname,
    conditionalReleaseDate = conditionalReleaseDate,
    paroleEligibilityDate = paroleEligibilityDate,
    tariffExpiryDate = tariffExpiryDate,
    earliestReleaseDate = earliestReleaseDate,
    earliestReleaseDateType = earliestReleaseDateType,
    prisonNumber = prisonNumber,
    indeterminateSentence = indeterminateSentence,
    nonDtoReleaseDateType = nonDtoReleaseDateType,
    sentenceType = sentenceType,
    location = location,
    gender = gender,
  )
}
