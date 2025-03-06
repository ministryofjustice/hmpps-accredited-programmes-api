package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "oasys_pni_result_temp")
data class OasysPniResultEntity(
  @Id
  val pniResultId: UUID,
  @Column
  val prisonNumber: String,
  @Column
  val oasysAssessmentId: Long?,
  @Column
  val programmePathway: String?,
)
