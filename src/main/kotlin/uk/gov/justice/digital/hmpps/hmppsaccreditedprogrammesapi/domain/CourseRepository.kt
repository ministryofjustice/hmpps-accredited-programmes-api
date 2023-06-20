package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import java.util.*

interface CourseRepository {
  fun allCourses(): List<CourseEntity>
  fun course(courseId: UUID): CourseEntity?
  fun offeringsForCourse(courseId: UUID): List<Offering>
  fun courseOffering(courseId: UUID, offeringId: UUID): Offering?
  fun allAudiences(): Set<Audience>
}
