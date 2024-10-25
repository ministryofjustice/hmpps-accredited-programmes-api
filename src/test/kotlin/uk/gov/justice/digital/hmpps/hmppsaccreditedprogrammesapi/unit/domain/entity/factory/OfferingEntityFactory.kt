package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import java.util.UUID
class OfferingEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var organisationId: String = randomAlphanumericString()
  private var organisation: OrganisationEntity = OrganisationEntityFactory().produce()
  private var contactEmail: String = randomEmailAddress()
  private var secondaryContactEmail: String? = null
  private var withdrawn: Boolean = false
  private var referable: Boolean = true

  fun withId(id: UUID?) = apply { this.id = id }
  fun withOrganisationId(organisationId: String) = apply { this.organisationId = organisationId }
  fun withOrganisation(organisation: OrganisationEntity) = apply { this.organisation = organisation }
  fun withContactEmail(contactEmail: String) = apply { this.contactEmail = contactEmail }
  fun withSecondaryContactEmail(secondaryContactEmail: String?) = apply { this.secondaryContactEmail = secondaryContactEmail }
  fun withWithdrawn(withdrawn: Boolean) = apply { this.withdrawn = withdrawn }

  fun produce() = OfferingEntity(
    id = this.id,
    organisationId = this.organisationId,
    organisation = this.organisation,
    contactEmail = this.contactEmail,
    secondaryContactEmail = this.secondaryContactEmail,
    withdrawn = this.withdrawn,
    referable = this.referable,
  )
}
