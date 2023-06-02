package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.ListCrudRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import java.util.UUID

interface CourseEntityRepository : ListCrudRepository<CourseEntity, UUID>
