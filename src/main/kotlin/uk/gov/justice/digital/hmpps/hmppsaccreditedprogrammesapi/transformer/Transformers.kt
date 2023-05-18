package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Prerequisite

fun CourseEntity.toApi(): Course = Course(
  id = this.id,
  name = this.name,
  type = this.type,
  description = this.description,
  coursePrerequisites = this.prerequisites.map(Prerequisite::toApi),
)

fun Prerequisite.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = this.name,
  description = this.description,
)

fun Offering.toApi(): CourseOffering = CourseOffering(
  id = this.id,
  organisationId = this.organisationId,
  duration = this.duration.toIsoString(),
  groupSize = this.groupSize,
  contactEmail = contactEmail,
)
