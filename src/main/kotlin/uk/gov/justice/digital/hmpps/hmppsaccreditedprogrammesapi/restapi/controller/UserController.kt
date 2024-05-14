package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.UserApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CaseLoad
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService

@Service
class UserController
@Autowired
constructor(
  private val personService: PersonService,
) : UserApiDelegate {
  override fun getCurrentUserCaseloads(allCaseloads: Boolean): ResponseEntity<List<CaseLoad>> = ResponseEntity.ok(
    personService
      .getCurrentUsersCaseloads(allCaseloads)
      .map {
        CaseLoad(
          caseLoadId = it.caseLoadId,
          description = it.description,
          type = it.type,
          caseloadFunction = it.caseloadFunction,
          currentlyActive = it.currentlyActive,
        )
      },
  )
}
