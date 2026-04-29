package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import java.util.UUID

@Repository
interface ReferralStatusHistoryRepository : JpaRepository<ReferralStatusHistoryEntity, UUID> {

  @EntityGraph(attributePaths = ["previousStatus", "status", "category", "reason"])
  fun getAllByReferralIdOrderByStatusStartDateDesc(referralId: UUID): List<ReferralStatusHistoryEntity>

  fun deleteAllByReferralIdIsIn(referralIds: List<UUID>)

  @EntityGraph(attributePaths = ["previousStatus", "status", "category", "reason"])
  @Query(
    """
    SELECT rsh FROM ReferralStatusHistoryEntity rsh
    JOIN ReferralEntity r ON rsh.referralId = r.id
    WHERE r.prisonNumber = :prisonNumber
    ORDER BY rsh.statusStartDate DESC
    """,
  )
  fun findByPrisonNumber(prisonNumber: String): List<ReferralStatusHistoryEntity>
}
