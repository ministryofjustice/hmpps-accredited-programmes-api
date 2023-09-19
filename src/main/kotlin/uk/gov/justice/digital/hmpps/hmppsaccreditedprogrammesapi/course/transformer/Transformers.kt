package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseAudience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.NewPrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.OfferingUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Prerequisite

fun CourseEntity.toApi(): Course = Course(
  id = id!!,
  name = name,
  description = description,
  alternateName = alternateName,
  coursePrerequisites = prerequisites.map(Prerequisite::toApi),
  audiences = audiences.map(Audience::toApi),
  referable = referable,
)

fun CourseEntity.toCourseRecord(): CourseRecord = CourseRecord(
  name = name,
  description = description ?: "",
  alternateName = alternateName,
  referable = referable,
  identifier = identifier,
  audience = audiences.joinToString { it.value },
)

fun Prerequisite.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = name,
  description = description,
)

fun Offering.toApi(): CourseOffering = CourseOffering(
  id = id!!,
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
  referable = referable,
)

fun OfferingRecord.toDomain(): OfferingUpdate = OfferingUpdate(
  prisonId = prisonId.trim(),
  identifier = identifier.trim(),
  contactEmail = contactEmail?.trim(),
  // The controller treats an absent value as an empty string. The Domain expects an absent value to be null.
  secondaryContactEmail = secondaryContactEmail?.let { it.trim().ifEmpty { null } },
)

fun PrerequisiteRecord.toDomain(): NewPrerequisite = NewPrerequisite(
  name = name,
  description = description,
  identifier = identifier,
)
