package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import java.util.UUID

@Repository
interface CourseRepository : JpaRepository<CourseEntity, UUID> {
  @Query(
    """
    SELECT c FROM CourseEntity c 
    JOIN FETCH c.offerings o 
    WHERE o.id = :offeringId
  """,
  )
  fun findByOfferingId(offeringId: UUID): CourseEntity?

  @Query(
    """
    SELECT distinct (c.name) FROM CourseEntity c
    WHERE ((:includeWithdrawn IS true OR c.withdrawn IS false) AND c.displayOnPgmdir IS TRUE)
    order by c.name asc 
  """,
  )
  fun getCourseNames(includeWithdrawn: Boolean?): List<String>

  fun findByIdentifier(identifier: String): CourseEntity?

  fun findAllByWithdrawnIsFalse(): List<CourseEntity>

  @Query(
    """
    SELECT c FROM CourseEntity c 
    JOIN FETCH c.offerings o 
    INNER JOIN OrganisationEntity org ON o.organisationId = org.code  
    WHERE c.id IN :courseIds
    AND c.audience = :audience
    AND org.gender = :gender
  """,
  )
  fun findBuildingChoicesCourses(courseIds: List<UUID>, audience: String, gender: String): List<CourseEntity>?
}
