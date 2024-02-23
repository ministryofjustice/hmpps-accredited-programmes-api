package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusTransitionRepository

@RestController
@RequestMapping("status-transition-diagram")
class StatusFlowDiagram(
  private val transitionRepository: ReferralStatusTransitionRepository,
  private val statusRepository: ReferralStatusRepository,
) {
  @GetMapping
  fun getStatusFlowDiagram(): String {
    val transitions = transitionRepository.findAll()
    val statuses = statusRepository.findAll()

    val nodeDefinitions = statuses.map { "${it.code} [shape=box fillcolor=\"${it.boxColour()}\"];" }
    val ptlines = transitions.filter { it.ptUser }.map { "${it.fromStatus.code} -> ${it.toStatus.code};" }
    val pomlines = transitions.filter { it.pomUser }.map { "${it.fromStatus.code} -> ${it.toStatus.code};" }

    val content =
      """
        ${nodeDefinitions.joinToString("\n")}
        edge [color=red];
        ${ptlines.joinToString("\n")}
        edge [color=blue];
        ${pomlines.joinToString("\n")}
      """.trimIndent()

    return """
      digraph StatusFlow {
      node [style=filled fillcolor="#f8f8f8"]
      subgraph legend {
        label = "Legend";
        PT [label="PT Transition", color=red, shape=box];
        POM [label="POM Transition", color=blue, shape=box];
        START [label="Start status", shape=box fillcolor="#DAF7A6"];
        END [label="End status", shape=box fillcolor="#FC6109"];
      }
      $content
      }
    """.trimIndent()
  }

  fun ReferralStatusEntity.boxColour(): String {
    if (draft) return "#DAF7A6"
    if (closed) return "#FC6109"
    return "#f8f8f8"
  }
}
