package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import java.math.BigInteger
import java.util.UUID

@Repository
interface StaffRepository : JpaRepository<StaffEntity, UUID> {

  fun findByStaffId(staffId: BigInteger): StaffEntity?

  @Query(
    """
    SELECT DISTINCT s FROM StaffEntity s 
    JOIN ReferralEntity r ON s.staffId = r.primaryPomStaffId OR s.staffId = r.secondaryPomStaffId 
    WHERE r.prisonNumber = :prisonNumber
  """,
  )
  fun findByPrisonNumber(prisonNumber: String): List<StaffEntity>
}
