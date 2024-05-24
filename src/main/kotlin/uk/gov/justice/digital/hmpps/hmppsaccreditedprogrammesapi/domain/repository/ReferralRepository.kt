package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.OffsetDateTime
import java.util.UUID

@Repository
interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {

  @EntityGraph(attributePaths = ["offering", "offering.course", "referrer"])
  @Query(
    """
        SELECT r FROM ReferralEntity r
        WHERE r.prisonNumber = :prisonerNumber
        AND (:fromDate IS NULL OR r.submittedOn >= :fromDate)
        AND (:toDate IS NULL OR r.submittedOn <= :toDate)
        """,
  )
  fun getSarReferrals(
    @Param("prisonerNumber") prisonerNumber: String,
    @Param("fromDate") fromDate: OffsetDateTime?,
    @Param("toDate") toDate: OffsetDateTime?,
  ): List<ReferralEntity>
}
