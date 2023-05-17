package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Course
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursePrerequisite
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.PrerequisiteEntity

fun CourseEntity.toApi(): Course = Course(
  id = this.id,
  name = this.name,
  type = this.type,
  description = this.description,
  coursePrerequisites = this.prerequisites.map(PrerequisiteEntity::toApi),
)

fun PrerequisiteEntity.toApi(): CoursePrerequisite = CoursePrerequisite(
  name = this.name,
  description = this.description,
)
