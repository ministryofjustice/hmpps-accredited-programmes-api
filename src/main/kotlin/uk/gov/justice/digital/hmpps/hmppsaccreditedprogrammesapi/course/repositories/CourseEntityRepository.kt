package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import java.util.UUID

@Repository
interface CourseEntityRepository : JpaRepository<CourseEntity, UUID> {
  fun findByMutableOfferings_id(offeringId: UUID): CourseEntity?
}
