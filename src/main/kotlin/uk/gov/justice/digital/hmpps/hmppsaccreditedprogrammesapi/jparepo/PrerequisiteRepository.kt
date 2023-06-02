package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.ListCrudRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.UUID

interface PrerequisiteRepository : ListCrudRepository<Prerequisite, UUID>
