package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomEmailAddress
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.util.UUID
class OfferingEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var organisationId: String = randomAlphanumericString()
  private var contactEmail: String = randomEmailAddress()
  private var secondaryContactEmail: String? = null
  private var withdrawn: Boolean = false
  private var referable: Boolean = true
  private var course: CourseEntity = CourseEntityFactory().produce()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withOrganisationId(organisationId: String) = apply { this.organisationId = organisationId }
  fun withContactEmail(contactEmail: String) = apply { this.contactEmail = contactEmail }
  fun withSecondaryContactEmail(secondaryContactEmail: String?) = apply { this.secondaryContactEmail = secondaryContactEmail }
  fun withWithdrawn(withdrawn: Boolean) = apply { this.withdrawn = withdrawn }

  fun produce() = OfferingEntity(
    id = this.id,
    organisationId = this.organisationId,
    contactEmail = this.contactEmail,
    secondaryContactEmail = this.secondaryContactEmail,
    withdrawn = this.withdrawn,
    referable = this.referable,

  ).apply { course = this@OfferingEntityFactory.course }
}
