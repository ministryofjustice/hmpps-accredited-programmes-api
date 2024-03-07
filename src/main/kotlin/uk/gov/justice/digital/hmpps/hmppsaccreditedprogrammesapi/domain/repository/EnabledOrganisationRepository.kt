package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EnabledOrganisation
import java.util.UUID

@Repository
interface EnabledOrganisationRepository : JpaRepository<EnabledOrganisation, UUID> {
  fun findEnabledOrganisationByCode(code: String): EnabledOrganisation?
}
