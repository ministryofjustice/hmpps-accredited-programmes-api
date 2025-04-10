package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OasysPniResultEntity
import java.util.*

@Repository
interface OasysPniResultEntityRepository : JpaRepository<OasysPniResultEntity, UUID>
