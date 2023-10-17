package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.repositories

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
  private val offeringRepository: OfferingRepository,
  private val audienceRepository: AudienceRepository,
) : CourseRepository {
  override fun getAllCourses(): List<CourseEntity> = courseRepository
    .findAll()
    .onEach {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun getCourseById(courseId: UUID): CourseEntity? = courseRepository
    .findById(courseId)
    .getOrNull()
    ?.also {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun getCourseByOfferingId(offeringId: UUID): CourseEntity? = courseRepository
    .findByMutableOfferings_id(offeringId)
    ?.also {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun getOfferingsCsv(): List<Offering> = offeringRepository
    .findAll()
    .onEach { Hibernate.initialize(it.course) }

  override fun getAllOfferingsByCourseId(courseId: UUID): List<Offering> = courseRepository
    .findById(courseId)
    .getOrNull()
    ?.offerings
    ?.toList() ?: emptyList()

  override fun getOfferingById(offeringId: UUID): Offering? = offeringRepository.findById(offeringId).getOrNull()

  override fun getAllAudiences(): Set<Audience> = audienceRepository.findAll().toSet()

  override fun saveCourse(courseEntity: CourseEntity) {
    courseRepository.save(courseEntity)
  }

  override fun saveAudiences(audiences: Set<Audience>) {
    audienceRepository.saveAll(audiences)
  }
}
