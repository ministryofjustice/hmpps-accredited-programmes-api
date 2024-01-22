package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysLearning(
  val workRelatedSkills: String?,
  val problemsReadWriteNum: String?,
  val learningDifficulties: String?,
  val qualifications: String?,
  val basicSkillsScore: String?,
)
