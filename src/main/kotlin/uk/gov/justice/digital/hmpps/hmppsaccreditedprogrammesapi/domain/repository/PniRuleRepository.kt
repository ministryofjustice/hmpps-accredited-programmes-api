package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PniRuleEntity
import java.util.UUID

@Repository
interface PniRuleRepository : JpaRepository<PniRuleEntity, UUID> {

  fun findPniRuleEntityByOverallNeedAndOverallRisk(overallNeed: String, overallRisk: String): PniRuleEntity?
}
