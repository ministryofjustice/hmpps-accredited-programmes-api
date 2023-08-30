package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import java.util.UUID

@Repository
interface CourseEntityRepository : JpaRepository<CourseEntity, UUID> {
  fun findByOfferings_id(offeringId: UUID): CourseEntity?

  // TODO: Meh. Change Offering from Embeddable to Entity and add OfferingRepository
  @Query("select o from CourseEntity c join c.offerings o where o.id = :offeringId")
  fun findOfferingById(offeringId: UUID): Offering?
}
