package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "pni_rule")
data class PniRuleEntity(

  @Id
  @GeneratedValue
  @Column(name = "rule_id")
  var id: UUID = UUID.randomUUID(),

  @Column
  var overallNeed: String,

  @Column
  var overallRisk: String,

  @Column
  var combinedPathway: String,
)
