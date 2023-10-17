package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import io.mockk.MockKMatcherScope

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
    offerings == other.offerings &&
    prerequisites == other.prerequisites &&
    referable == other.referable &&
    withdrawn == other.withdrawn

fun Set<AudienceEntity>.refEq(other: Set<AudienceEntity>) =
  this.size == other.size &&
    this.all { x -> other.any { y -> x === y } }
