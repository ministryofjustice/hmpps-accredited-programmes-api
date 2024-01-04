package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<OfferingEntity, UUID> {
  @Query(
    """
    SELECT o FROM OfferingEntity o 
    JOIN FETCH o.course 
    WHERE o.course.id = :courseId
  """,
  )
  fun findAllByCourseId(courseId: UUID): List<OfferingEntity>
}
