package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import java.util.UUID

class OfferingEntityFactory : Factory<OfferingEntity> {
  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var organisationId: Yielded<String> = { randomAlphanumericString() }
  private var contactEmail: Yielded<String> = { randomEmailAddress() }
  private var secondaryContactEmail: Yielded<String?> = { null }
  private var withdrawn: Yielded<Boolean> = { false }
  private var referable: Yielded<Boolean> = { true }

  fun withId(id: UUID?) = apply {
    this.id = { id }
  }

  fun withOrganisationId(organisationId: String) = apply {
    this.organisationId = { organisationId }
  }

  fun withContactEmail(contactEmail: String) = apply {
    this.contactEmail = { contactEmail }
  }

  fun withSecondaryContactEmail(secondaryContactEmail: String?) = apply {
    this.secondaryContactEmail = { secondaryContactEmail }
  }

  fun withWithdrawn(withdrawn: Boolean) = apply {
    this.withdrawn = { withdrawn }
  }

  override fun produce() = OfferingEntity(
    id = this.id(),
    organisationId = this.organisationId(),
    contactEmail = this.contactEmail(),
    secondaryContactEmail = this.secondaryContactEmail(),
    withdrawn = this.withdrawn(),
    referable = this.referable(),
  )
}

class OfferingUpdateFactory : Factory<OfferingUpdate> {
  private var prisonId: Yielded<String> = { PRISON_NUMBER_1 }
  private var identifier: Yielded<String> = { randomUppercaseString() }
  private var contactEmail: Yielded<String?> = { null }
  private var secondaryContactEmail: Yielded<String?> = { null }
  private var referable: Yielded<Boolean> = { true }

  fun withPrisonId(prisonId: String) = apply {
    this.prisonId = { prisonId }
  }

  fun withIdentifier(identifier: String) = apply {
    this.identifier = { identifier }
  }

  fun withContactEmail(contactEmail: String?) = apply {
    this.contactEmail = { contactEmail }
  }

  override fun produce() = OfferingUpdate(
    prisonId = this.prisonId(),
    identifier = this.identifier(),
    contactEmail = this.contactEmail(),
    secondaryContactEmail = this.secondaryContactEmail(),
    referable = this.referable(),
  )
}
