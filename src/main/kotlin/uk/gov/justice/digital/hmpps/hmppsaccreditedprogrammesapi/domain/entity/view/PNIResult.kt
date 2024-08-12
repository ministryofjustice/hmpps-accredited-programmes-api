package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "pni_result")
data class PniResultEntity(
  @Id
  val pniResultId: UUID = UUID.randomUUID(),
  @Column
  val prisonNumber: String,
  @Column
  val crn: String?,
  @Column
  val referralId: UUID?,
  @Column
  val oasysAssessmentId: Long?,
  @Column
  val oasysAssessmentCompletedDate: LocalDateTime?,
  @Column
  val programmePathway: String?,
  @Column
  val needsClassification: String?,
  @Column
  val overallNeedsScore: Int?,
  @Column
  val riskClassification: String?,
  @Column
  val pniAssessmentDate: LocalDateTime?,
  @Column
  val pniValid: Boolean,
  @Column
  val pniResultJson: String?,
)

@Repository
interface PNIResultEntityRepository : JpaRepository<PniResultEntity, UUID> {
  fun findAllByPrisonNumber(prisonNumber: String): List<PniResultEntity>
}
