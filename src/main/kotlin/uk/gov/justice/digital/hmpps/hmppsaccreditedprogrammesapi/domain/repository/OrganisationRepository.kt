package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import java.util.UUID

@Repository
interface OrganisationRepository : JpaRepository<OrganisationEntity, UUID> {

  fun findOrganisationEntityByCode(code: String): OrganisationEntity?
}
