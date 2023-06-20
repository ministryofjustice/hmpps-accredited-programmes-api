package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

interface MutableCourseRepository : CourseRepository {
  fun clear()
  fun saveCourse(courseEntity: CourseEntity)
  fun saveAudiences(audiences: Set<Audience>)
}
