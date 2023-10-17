package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseAudience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OfferingRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PrerequisiteEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.CourseUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.PrerequisiteUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update.OfferingUpdate

fun CourseEntity.toApi(): Course = Course(
  id = id!!,
  name = name,
  description = description,
  alternateName = alternateName,
  coursePrerequisites = prerequisites.map(PrerequisiteEntity::toApi),
  audiences = audiences.map(AudienceEntity::toApi),
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

fun PrerequisiteEntity.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = name,
  description = description,
)

fun OfferingEntity.toApi(): CourseOffering = CourseOffering(
  id = id!!,
  organisationId = organisationId,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
)

fun OfferingEntity.toOfferingRecord() = OfferingRecord(
  course = course.name,
  identifier = course.identifier,
  prisonId = organisationId,
  contactEmail = contactEmail,
  secondaryContactEmail = secondaryContactEmail,
)

fun AudienceEntity.toApi(): CourseAudience = CourseAudience(
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

fun PrerequisiteRecord.toDomain(): PrerequisiteUpdate = PrerequisiteUpdate(
  name = name,
  description = description,
  identifier = identifier,
)
