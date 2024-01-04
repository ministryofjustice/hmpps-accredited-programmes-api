package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity

import io.mockk.MockKMatcherScope
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity

fun MockKMatcherScope.eqCourse(course: CourseEntity?) = match<CourseEntity> {
  when (course) {
    null -> false
    else -> it.eqByFields(course)
  }
}

fun CourseEntity.eqByFields(other: CourseEntity) =
  name == other.name &&
    description == other.description &&
    audiences.refEq(other.audiences) &&
    mutableOfferings == other.mutableOfferings &&
    prerequisites == other.prerequisites &&
    referable == other.referable &&
    withdrawn == other.withdrawn

fun Set<AudienceEntity>.refEq(other: Set<AudienceEntity>) =
  this.size == other.size &&
    this.all { x -> other.any { y -> x === y } }
