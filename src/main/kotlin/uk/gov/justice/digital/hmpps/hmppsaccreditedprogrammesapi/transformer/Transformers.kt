package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseAudience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.PrerequisiteUpdate

fun CourseEntity.toApi(): Course = Course(
  id = id!!,
  name = name,
  description = description,
  alternateName = alternateName,
  coursePrerequisites = prerequisites.map(Prerequisite::toApi),
  audiences = audiences.map(Audience::toApi),
)

fun Prerequisite.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = name,
  description = description,
)

fun Offering.toApi(): CourseOffering = CourseOffering(
  id = id,
  organisationId = organisationId,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
)

fun Audience.toApi(): CourseAudience = CourseAudience(
  id = id!!,
  value = value,
)

fun CourseRecord.toDomain(): CourseUpdate = CourseUpdate(
  name = name.trim(),
  identifier = identifier.trim(),
  description = description.trim(),
  audience = audience,
  alternateName = alternateName?.trim(),
)

fun OfferingRecord.toDomain(): OfferingUpdate = OfferingUpdate(
  prisonId = prisonId.trim(),
  identifier = identifier.trim(),
  contactEmail = contactEmail?.trim(),
  // The controller treats an absent value as an empty string. The Domain expects an absent value to be null.
  secondaryContactEmail = secondaryContactEmail?.let { it.trim().ifEmpty { null } },
)

fun PrerequisiteRecord.toDomain(): PrerequisiteUpdate = PrerequisiteUpdate(
  name = name,
  description = description,
  identifier = identifier,
)
