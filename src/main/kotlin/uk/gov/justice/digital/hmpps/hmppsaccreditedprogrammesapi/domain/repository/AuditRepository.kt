package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditEntity
import java.util.UUID

@Repository
interface AuditRepository : JpaRepository<AuditEntity, UUID> {
  @Query(
    """
    SELECT a 
    FROM AuditEntity a
    WHERE a.prisonNumber = :prisonerNumber
    """,
  )
  fun getSarAuditRecords(
    @Param("prisonerNumber") prisonerNumber: String,
  ): List<AuditEntity>
}
