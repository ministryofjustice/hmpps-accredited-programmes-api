package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import java.time.LocalDate
import java.util.UUID

class PersonEntityFactory : Factory<PersonEntity> {
  private var id: Yielded<UUID> = { UUID.randomUUID() }
  private var forename: Yielded<String> = { randomAlphanumericString() }
  private var surname: Yielded<String> = { randomAlphanumericString() }
  private var prisonNumber: Yielded<String> = { randomPrisonNumber() }
  private var conditionalReleaseDate: Yielded<LocalDate> = { LocalDate.now() }
  private var paroleEligibilityDate: Yielded<LocalDate> = { LocalDate.now() }
  private var tariffExpiryDate: Yielded<LocalDate> = { LocalDate.now() }
  private var earliestReleaseDateType: Yielded<String> = { randomAlphanumericString() }
  private var earliestReleaseDate: Yielded<LocalDate> = { LocalDate.now() }
  private var indeterminateSentence: Yielded<Boolean> = { false }
  private var nonDtoReleaseDateType: Yielded<String> = { randomAlphanumericString() }
  private var sentenceType: Yielded<String> = { randomAlphanumericString() }
  private var location: Yielded<String> = { randomAlphanumericString() }
  private var gender: Yielded<String> = { "Male" }

  override fun produce() = PersonEntity(
    id = id(),
    forename = this.forename(),
    surname = this.surname(),
    conditionalReleaseDate = conditionalReleaseDate(),
    paroleEligibilityDate = paroleEligibilityDate(),
    tariffExpiryDate = tariffExpiryDate(),
    earliestReleaseDate = earliestReleaseDate(),
    earliestReleaseDateType = earliestReleaseDateType(),
    prisonNumber = prisonNumber(),
    indeterminateSentence = indeterminateSentence(),
    nonDtoReleaseDateType = nonDtoReleaseDateType(),
    sentenceType = sentenceType(),
    location = location(),
    gender = gender(),
  )
}
