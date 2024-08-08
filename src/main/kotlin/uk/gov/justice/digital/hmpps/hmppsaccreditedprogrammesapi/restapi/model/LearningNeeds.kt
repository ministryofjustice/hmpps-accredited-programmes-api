package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param noFixedAbodeOrTransient
 * @param workRelatedSkills
 * @param problemsReadWriteNum
 * @param learningDifficulties
 * @param qualifications
 * @param basicSkillsScore
 * @param basicSkillsScoreDescription
 */
data class LearningNeeds(

  @Schema(example = "null", description = "")
  @get:JsonProperty("noFixedAbodeOrTransient") val noFixedAbodeOrTransient: Boolean? = false,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("workRelatedSkills") val workRelatedSkills: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("problemsReadWriteNum") val problemsReadWriteNum: String? = null,

  @Schema(example = "0-No problems", description = "")
  @get:JsonProperty("learningDifficulties") val learningDifficulties: String? = null,

  @Schema(example = "0-Any qualifications", description = "")
  @get:JsonProperty("qualifications") val qualifications: String? = null,

  @Schema(example = "6", description = "")
  @get:JsonProperty("basicSkillsScore") val basicSkillsScore: String? = null,

  @Schema(example = "free text about this persons learning needs", description = "")
  @get:JsonProperty("basicSkillsScoreDescription") val basicSkillsScoreDescription: String? = null,
)
