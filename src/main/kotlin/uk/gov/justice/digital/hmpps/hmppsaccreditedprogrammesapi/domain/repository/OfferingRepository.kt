package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<OfferingEntity, UUID> {
  fun findAllByCourseId(courseId: UUID): List<OfferingEntity>
  fun findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId: UUID, organisationId: String): OfferingEntity?

  fun findByCourseIdAndIdAndWithdrawnIsFalse(courseId: UUID, offeringId: UUID): OfferingEntity?
}
