package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import java.util.*

@Repository
interface PNIResultEntityRepository : JpaRepository<PniResultEntity, UUID> {
  fun findAllByPrisonNumber(prisonNumber: String): List<PniResultEntity>

}
