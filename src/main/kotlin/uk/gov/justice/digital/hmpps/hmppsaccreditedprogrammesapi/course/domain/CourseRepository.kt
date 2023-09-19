package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import java.util.UUID

interface CourseRepository {
  fun allCourses(): List<CourseEntity>
  fun course(courseId: UUID): CourseEntity?
  fun findCourseByOfferingId(offeringId: UUID): CourseEntity?
  fun saveCourse(courseEntity: CourseEntity)
  fun offeringsForCourse(courseId: UUID): List<Offering>
  fun courseOffering(courseId: UUID, offeringId: UUID): Offering?
  fun courseOffering(offeringId: UUID): Offering?
  fun allAudiences(): Set<Audience>
  fun saveAudiences(audiences: Set<Audience>)
}
