package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Audience
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.MutableCourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
@Qualifier("JPA")
class JpaCourseRepository
@Autowired
constructor(
  private val courseRepository: CourseEntityRepository,
  private val audienceRepository: AudienceRepository,
) : MutableCourseRepository {
  override fun allCourses(): List<CourseEntity> = courseRepository.findAll()

  override fun course(courseId: UUID): CourseEntity? = courseRepository.findById(courseId).getOrNull()

  override fun offeringsForCourse(courseId: UUID): List<Offering> = course(courseId)?.offerings?.toList() ?: emptyList()

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = offeringsForCourse(courseId).find { it.id == offeringId }

  override fun allAudiences(): Set<Audience> = audienceRepository.findAll().toSet()

  override fun clear() {
    courseRepository.deleteAll()
    audienceRepository.deleteAll()
  }

  override fun saveCourse(courseEntity: CourseEntity) {
    courseRepository.save(courseEntity)
  }

  override fun saveAudiences(audiences: Set<Audience>) {
    audienceRepository.saveAll(audiences)
  }
}
