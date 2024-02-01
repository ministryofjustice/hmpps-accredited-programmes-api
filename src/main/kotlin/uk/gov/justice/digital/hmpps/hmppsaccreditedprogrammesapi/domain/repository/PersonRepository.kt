package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import java.util.UUID

@Repository
interface PersonRepository : JpaRepository<PersonEntity, UUID> {

  fun findPersonEntityByPrisonNumber(prisonerNumber: String): PersonEntity?
}
