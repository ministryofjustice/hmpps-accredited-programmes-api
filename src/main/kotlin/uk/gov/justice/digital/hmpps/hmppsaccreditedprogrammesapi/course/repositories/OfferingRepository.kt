package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingEntity
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<OfferingEntity, UUID>
