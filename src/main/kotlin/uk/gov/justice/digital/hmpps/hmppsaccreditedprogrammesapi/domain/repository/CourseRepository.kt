package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.util.UUID

interface CourseRepository {
  fun getAllCourses(): List<CourseEntity>
  fun getCourseById(courseId: UUID): CourseEntity?
  fun getCourseByOfferingId(offeringId: UUID): CourseEntity?
  fun saveCourse(courseEntity: CourseEntity)
  fun getAllOfferings(): List<OfferingEntity>
  fun getAllOfferingsByCourseId(courseId: UUID): List<OfferingEntity>
  fun getOfferingById(offeringId: UUID): OfferingEntity?
  fun getAllAudiences(): Set<AudienceEntity>
  fun saveAudiences(audiences: Set<AudienceEntity>)
}
