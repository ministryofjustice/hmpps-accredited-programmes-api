package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AddressEntity
import java.util.UUID

class AddressEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var addressLine1: String = randomAlphanumericString()
  private var addressLine2: String = randomAlphanumericString()
  private var country: String = randomAlphanumericString()
  private var county: String = randomAlphanumericString()
  private var postalCode: String = randomAlphanumericString()
  private var town: String = randomAlphanumericString()

  fun withId(id: UUID) = apply { this.id = id }
  fun withAddressLine1(addressLine1: String) = apply { this.addressLine1 = addressLine1 }
  fun withAddressLine2(addressLine2: String) = apply { this.addressLine2 = addressLine2 }
  fun withCountry(country: String) = apply { this.country = country }
  fun withCounty(county: String) = apply { this.county = county }
  fun withPostalCode(postalCode: String) = apply { this.postalCode = postalCode }
  fun withTown(town: String) = apply { this.town = town }

  fun produce() = AddressEntity(
    id = this.id,
    addressLine1 = this.addressLine1,
    addressLine2 = this.addressLine2,
    country = this.country,
    county = this.county,
    postalCode = this.postalCode,
    town = this.town,
  )
}
