package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
class JpaCourseRepository
@Autowired
constructor(
  private val courseRepository: CourseEntityRepository,
  private val audienceRepository: AudienceRepository,
) : CourseRepository {
  override fun allCourses(): List<CourseEntity> = courseRepository
    .findAll()
    .onEach {
      Hibernate.initialize(it.audiences)
      Hibernate.initialize(it.prerequisites)
    }

  override fun allActiveCourses(): List<CourseEntity> = courseRepository
    .findAllByWithdrawn(false)
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

  override fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository
    .findById(courseId)
    .getOrNull()
    ?.offerings
    ?.toList() ?: emptyList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = offeringsForCourse(courseId).find { it.id == offeringId }

  override fun allAudiences(): Set<Audience> = audienceRepository.findAll().toSet()

  override fun saveCourse(courseEntity: CourseEntity) {
    courseRepository.save(courseEntity)
  }

  override fun saveAudiences(audiences: Set<Audience>) {
    audienceRepository.saveAll(audiences)
  }
}
