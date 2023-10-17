package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import java.util.UUID

interface CourseRepository {
  fun getAllCourses(): List<CourseEntity>
  fun getCourseById(courseId: UUID): CourseEntity?
  fun getCourseByOfferingId(offeringId: UUID): CourseEntity?
  fun saveCourse(courseEntity: CourseEntity)
  fun getOfferingsCsv(): List<Offering>
  fun getAllOfferingsByCourseId(courseId: UUID): List<Offering>
  fun getOfferingById(offeringId: UUID): Offering?
  fun getAllAudiences(): Set<Audience>
  fun saveAudiences(audiences: Set<Audience>)
}
