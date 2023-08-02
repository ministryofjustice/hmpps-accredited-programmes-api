package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience
import java.util.UUID

interface AudienceRepository : JpaRepository<Audience, UUID>
