package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.controller

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.ProgrammesApiDelegate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Programme
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ProgrammePrerequisite
import java.util.*

@Service
class ProgrammesController : ProgrammesApiDelegate {
  override fun programmesGet(): ResponseEntity<List<Programme>> {
    return ResponseEntity.ok(
      listOf(
        Programme(
          id = UUID.randomUUID(),
          name = "Thinking Skills Programme",
          programmeType = "TypeA",
          description = "Thinking Skills Programme (TSP) is for adult men and women with a medium or high risk of re offending...",
          programmePrerequisites = listOf(
            ProgrammePrerequisite(key = "gender", value = "female"),
            ProgrammePrerequisite(key = "risk score", value = "ORGS: 50+"),
            ProgrammePrerequisite(key = "offence type", value = "some offence here"),
          ),
        ),
        Programme(
          id = UUID.randomUUID(),
          name = "Becoming new me +",
          programmeType = "TypeB",
          description = "Becoming new me + is for adult men and women with a medium or high risk of re offending...",
          programmePrerequisites = listOf(
            ProgrammePrerequisite(key = "gender", value = "female"),
            ProgrammePrerequisite(key = "risk score", value = "ORGS: 50+"),
            ProgrammePrerequisite(key = "offence type", value = "some offence here"),
          ),
        ),
        Programme(
          id = UUID.randomUUID(),
          name = "New me strengths",
          programmeType = "TypeA",
          description = "New me strengths is for adult men and women with a medium or high risk of re offending...",
          programmePrerequisites = listOf(
            ProgrammePrerequisite(key = "gender", value = "female"),
            ProgrammePrerequisite(key = "risk score", value = "ORGS: 50+"),
            ProgrammePrerequisite(key = "offence type", value = "some offence here"),
          ),
        ),
      ),
    )
  }
}
