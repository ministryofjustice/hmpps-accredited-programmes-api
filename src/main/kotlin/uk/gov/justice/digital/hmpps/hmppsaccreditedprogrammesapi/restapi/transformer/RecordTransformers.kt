package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrerequisiteRecord

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

fun CourseEntity.toCourseRecord(): CourseRecord = CourseRecord(
  name = name,
  description = description ?: "",
  alternateName = alternateName,
  identifier = identifier,
  audience = audience,
  audienceColour = audienceColour,
)

fun PrerequisiteEntity.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = name,
  description = description,
)

fun OfferingEntity.toApi(orgEnabled: Boolean): CourseOffering = CourseOffering(
  id = id!!,
  organisationId = organisationId,
  organisationEnabled = orgEnabled,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
  referable = referable,
)

fun OfferingEntity.toApi(): CourseOffering = CourseOffering(
  id = id!!,
  organisationId = organisationId,
  organisationEnabled = referable,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
  referable = referable,
)

fun OfferingEntity.toOfferingRecord() = OfferingRecord(
  course = course.name,
  identifier = course.identifier,
  prisonId = organisationId,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
  referable = referable,
)

fun CourseRecord.toDomain(): CourseUpdate = CourseUpdate(
  name = name.trim(),
  identifier = identifier.trim(),
  description = description.trim(),
  audience = audience,
  audienceColour = audienceColour?.let { it.trim().ifEmpty { null } },
  alternateName = alternateName?.trim(),
)

fun OfferingRecord.toDomain(): OfferingUpdate = OfferingUpdate(
  prisonId = prisonId.trim(),
  identifier = identifier.trim(),
  contactEmail = contactEmail?.trim(),
  // The controller treats an absent value as an empty string. The Domain expects an absent value to be null.
  secondaryContactEmail = secondaryContactEmail?.let { it.trim().ifEmpty { null } },
  referable = referable,
)

fun PrerequisiteRecord.toDomain(): NewPrerequisite = NewPrerequisite(
  name = name,
  description = description,
  identifier = identifier,
)
