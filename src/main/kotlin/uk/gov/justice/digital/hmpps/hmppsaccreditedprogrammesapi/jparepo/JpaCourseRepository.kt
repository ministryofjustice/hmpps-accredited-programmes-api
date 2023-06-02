package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.jparepo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.Offering
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Component
@Qualifier("JPA")
class JpaCourseRepository
@Autowired
constructor(
  private val courseEntityRepository: CourseEntityRepository,
  private val offeringRepository: OfferingRepository,
) : CourseRepository {
  override fun allCourses(): List<CourseEntity> = courseEntityRepository
    .findAll()

  override fun course(courseId: UUID): CourseEntity? = courseEntityRepository
    .findById(courseId).getOrNull()

  override fun offeringsForCourse(courseId: UUID): List<Offering> = offeringRepository
    .findByCourseId(courseId)

  override fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = offeringRepository
    .findById(offeringId)
    .filter { it.course.id == courseId }
    .getOrNull()
}
