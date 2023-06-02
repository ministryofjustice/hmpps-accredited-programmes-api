package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.ListCrudRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import java.util.UUID

interface OfferingRepository : ListCrudRepository<Offering, UUID> {
  fun findByCourseId(courseId: UUID): List<Offering>
}
