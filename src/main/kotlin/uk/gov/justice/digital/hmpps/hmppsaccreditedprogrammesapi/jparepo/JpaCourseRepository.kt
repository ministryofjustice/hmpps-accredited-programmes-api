package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.MutableCourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Component
class JpaCourseRepository
@Autowired
constructor(
  private val courseRepository: CourseEntityRepository,
  private val audienceRepository: AudienceRepository,
  private val entityManager: EntityManager,
) : MutableCourseRepository {
  override fun allCourses(): List<CourseEntity> = courseRepository.findAll()

  override fun course(courseId: UUID): CourseEntity? = courseRepository.findById(courseId).getOrNull()

  override fun offeringsForCourse(courseId: UUID): List<Offering> = course(courseId)?.offerings?.toList() ?: emptyList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = offeringsForCourse(courseId).find { it.id == offeringId }

  override fun allAudiences(): Set<Audience> = audienceRepository.findAll().toSet()

  override fun clear() {
    courseRepository.deleteAll()
    audienceRepository.deleteAll()
    /*
     By default, Hibernate lazily deletes audience entities after inserting new ones which can violate the unique
     constraint on audience.audience_value
     The call to flush() instructs Hibernate to perform all pending databases changes immediately and ensures that
     the deletes requested above happen before any subsequent inserts or updates.
     */
    entityManager.flush()
  }

  override fun saveCourse(courseEntity: CourseEntity) {
    courseRepository.save(courseEntity)
  }

  override fun saveAudiences(audiences: Set<Audience>) {
    audienceRepository.saveAll(audiences)
  }
}
