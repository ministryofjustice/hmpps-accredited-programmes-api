package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.ListCrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import java.util.UUID

@Repository
interface CourseEntityRepository : ListCrudRepository<CourseEntity, UUID>
