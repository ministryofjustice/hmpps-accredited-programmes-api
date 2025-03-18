package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import java.util.UUID

@Repository
interface ReferralStatusHistoryRepository : JpaRepository<ReferralStatusHistoryEntity, UUID> {

    @EntityGraph(attributePaths = ["previousStatus", "status", "category", "reason"])
    fun getAllByReferralIdOrderByStatusStartDateDesc(referralId: UUID): List<ReferralStatusHistoryEntity>
}