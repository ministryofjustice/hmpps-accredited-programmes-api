package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import java.util.UUID

@Repository
interface OrganisationRepository : JpaRepository<OrganisationEntity, UUID> {

  fun findOrganisationEntityByCode(code: String): OrganisationEntity?

  fun findOrganisationEntityByName(name: String): OrganisationEntity?

  /**
   * Batch-load organisations by their business code. Preferred over calling
   * [findOrganisationEntityByCode] in a loop when resolving multiple codes
   * (e.g. building the SAR content payload) as it collapses N point-lookups
   * into a single `WHERE code IN (?)` query. Unmatched codes are silently
   * dropped by the IN clause; callers should treat a missing entry in the
   * `associateBy { it.code }` map as "code not found".
   */
  fun findAllByCodeIn(codes: Collection<String>): List<OrganisationEntity>
}
