package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.testsupport.randomStringMultiCaseWithNumbers
import java.util.UUID

class OfferingEntityFactory : Factory<Offering> {
  private var id: Yielded<UUID> = { UUID.randomUUID() }
  private var organisationId: Yielded<String> = { randomStringMultiCaseWithNumbers(6) }
  private var contactEmail: Yielded<String> = { randomEmailAddress() }
  private var secondaryContactEmail: Yielded<String> = { randomEmailAddress() }

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withOrganisationId(organisationId: String) = apply {
    this.organisationId = { organisationId }
  }

  fun withContactEmail(contactEmail: String) = apply {
    this.contactEmail = { contactEmail }
  }

  fun withSecondaryContactEmail(secondaryContactEmail: String) = apply {
    this.secondaryContactEmail = { secondaryContactEmail }
  }

  override fun produce(): Offering = Offering(
    id = this.id(),
    organisationId = this.organisationId(),
    contactEmail = this.contactEmail(),
    secondaryContactEmail = this.secondaryContactEmail(),
  )
}
