package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.util.UUID

@Repository
interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {

  @EntityGraph(attributePaths = ["offering", "offering.course", "referrer"])
  @Query(
    """
        SELECT r FROM ReferralEntity r
        WHERE r.prisonNumber = :prisonerNumber
        """,
  )
  fun getSarReferrals(
    @Param("prisonerNumber") prisonerNumber: String,
  ): List<ReferralEntity>

  fun countAllByOfferingId(id: UUID): Long

  fun getReferralEntitiesByOfferingIdAndPrisonNumberAndStatusIn(offeringId: UUID, prisonerNumber: String, status: List<String>): List<ReferralEntity>?

  @Query("SELECT DISTINCT r.prisonNumber FROM ReferralEntity r where r.primaryPomStaffId is null")
  fun findAllDistinctPrisonNumbersWithoutPrimaryPom(): List<String>

  @Query("SELECT DISTINCT r.prisonNumber FROM ReferralEntity r where r.hasLdc is null")
  fun findAllDistinctPrisonNumbersWithoutLdc(): List<String>

  fun findAllByPrisonNumber(prisonNumber: String): List<ReferralEntity>
  fun findAllByPrisonNumberAndStatusIn(prisonNumber: String, openReferralStatus: List<String>): List<ReferralEntity>
}
