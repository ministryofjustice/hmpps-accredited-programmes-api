package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EligibilityOverrideReasonEntity
import java.util.*

@Repository
interface EligibilityOverrideReasonEntityRepository : JpaRepository<EligibilityOverrideReasonEntity, UUID>
