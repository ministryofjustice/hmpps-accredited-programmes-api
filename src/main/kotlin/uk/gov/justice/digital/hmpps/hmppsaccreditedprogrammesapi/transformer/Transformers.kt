package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseAudience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

fun CourseEntity.toApi(): Course = Course(
  id = id!!,
  name = name,
  description = description,
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
)

fun Audience.toApi(): CourseAudience = CourseAudience(
  id = id!!,
  value = value,
)
