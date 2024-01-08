package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import java.util.*

@Repository
interface CourseRepository : JpaRepository<CourseEntity, UUID> {

  @Query(
    """
    SELECT c FROM CourseEntity c 
    JOIN FETCH c.audiences 
    JOIN FETCH c.offerings 
    WHERE c.id = :courseId
  """,
  )
  override fun findById(courseId: UUID): Optional<CourseEntity>

  @Query(
    """
    SELECT c FROM CourseEntity c 
    JOIN FETCH c.offerings o 
    WHERE o.id = :offeringId
  """,
  )
  fun findByOfferingId(offeringId: UUID): CourseEntity?
}
