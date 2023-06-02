package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.ListCrudRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import java.util.UUID

interface AudienceRepository : ListCrudRepository<Audience, UUID>
