package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OasysPniResultEntity
import java.util.*

@Repository
interface OasysPniResultEntityRepository : JpaRepository<OasysPniResultEntity, UUID> {
  /**
   * Batch load of every OASys-PNI-result row for [prisonNumber], used by the
   * SAR (Subject Access Request) custody report.
   *
   * `ORDER BY o.oasysAssessmentId NULLS LAST, o.pniResultId` guarantees
   * deterministic ordering across runs so the SAR contract-test golden
   * snapshot doesn't flake when a subject has more than one OASys PNI result.
   * `oasysAssessmentId` is the natural OASys-side ordering; `pniResultId`
   * is the primary-key tie-break for same-assessment-id (or both-null) rows.
   */
  @Query(
    """
    SELECT o FROM OasysPniResultEntity o
    WHERE o.prisonNumber = :prisonNumber
    ORDER BY o.oasysAssessmentId NULLS LAST, o.pniResultId
    """,
  )
  fun findAllByPrisonNumber(prisonNumber: String): List<OasysPniResultEntity>
}
