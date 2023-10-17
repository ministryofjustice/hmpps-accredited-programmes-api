package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import java.util.UUID

@Repository
interface JpaCourseEntityRepository : JpaRepository<CourseEntity, UUID> {
  fun findByMutableOfferingsId(offeringId: UUID): CourseEntity?
}
