package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import java.util.*

interface PrerequisiteRepository : CrudRepository<Prerequisite, UUID>
