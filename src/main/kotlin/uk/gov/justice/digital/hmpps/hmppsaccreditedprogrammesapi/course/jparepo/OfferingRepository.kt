package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<Offering, UUID>
