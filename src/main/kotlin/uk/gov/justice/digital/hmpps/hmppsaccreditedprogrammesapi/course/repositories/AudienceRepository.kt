package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.AudienceEntity
import java.util.UUID

interface AudienceRepository : JpaRepository<AudienceEntity, UUID>
