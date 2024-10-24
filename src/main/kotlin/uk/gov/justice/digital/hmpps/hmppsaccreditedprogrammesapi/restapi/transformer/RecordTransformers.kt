package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Gender

fun CourseEntity.toApi(): Course = Course(
  id = id!!,
  identifier = identifier,
  name = name,
  description = description,
  alternateName = alternateName,
  coursePrerequisites = prerequisites.map(PrerequisiteEntity::toApi),
  audience = audience,
  audienceColour = audienceColour,
  displayName = name + addAudience(name, audience),
  withdrawn = withdrawn,
)

fun addAudience(name: String, audience: String): String {
  val courseNamesWithAudience = listOf("Kaizen", "New Me Strengths", "Becoming New Me Plus")
  return if (name in courseNamesWithAudience) {
    ": ${audience.lowercase()}"
  } else {
    ""
  }
}

fun PrerequisiteEntity.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = name,
  description = description,
)

fun OfferingEntity.toApi(orgEnabled: Boolean): CourseOffering = CourseOffering(
  id = id!!,
  organisationId = organisation.code,
  organisationEnabled = orgEnabled,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
  referable = referable,
  gender = Gender.valueOf(organisation.gender),
)

fun OfferingEntity.toApi(): CourseOffering = CourseOffering(
  id = id!!,
  organisationId = organisation.code,
  organisationEnabled = referable,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
  referable = referable,
  gender = Gender.valueOf(organisation.gender),
)
