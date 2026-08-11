package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import java.util.*

@Repository
interface PniResultRepository : JpaRepository<PniResultEntity, UUID> {
  /**
   * Batch load of every PNI-result row for [prisonNumber], used by the SAR
   * (Subject Access Request) custody report.
   *
   * `ORDER BY p.pniAssessmentDate NULLS LAST, p.pniResultId` guarantees
   * deterministic ordering across runs so the SAR contract-test golden
   * snapshot doesn't flake when a subject has more than one PNI result.
   * Chronological ascending is the natural read for a vettor;
   * `p.pniResultId` is the primary-key tie-break for same-date rows.
   */
  @Query(
    """
    SELECT p FROM PniResultEntity p
    WHERE p.prisonNumber = :prisonNumber
    ORDER BY p.pniAssessmentDate NULLS LAST, p.pniResultId
    """,
  )
  fun findAllByPrisonNumber(prisonNumber: String): List<PniResultEntity>

  fun findByReferralIdAndPrisonNumber(referralId: UUID, prisonNumber: String): PniResultEntity?
}
