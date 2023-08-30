package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import jakarta.persistence.EntityManager
import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class JpaCourseRepository
@Autowired
constructor(
  private val courseRepository: CourseEntityRepository,
  private val audienceRepository: AudienceRepository,
  private val entityManager: EntityManager,
) : CourseRepository {
  override fun allCourses(): List<CourseEntity> = courseRepository
    .findAll()
    .onEach {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun course(courseId: UUID): CourseEntity? = courseRepository
    .findById(courseId)
    .getOrNull()
    ?.also {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun findCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository
    .findByOfferings_id(offeringId)
    ?.also {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository
    .findById(courseId)
    .getOrNull()
    ?.offerings
    ?.toList() ?: emptyList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = offeringsForCourse(courseId).find { it.id == offeringId }
  override fun courseOffering(offeringId: UUID): Offering? = courseRepository.findOfferingById(offeringId)

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
