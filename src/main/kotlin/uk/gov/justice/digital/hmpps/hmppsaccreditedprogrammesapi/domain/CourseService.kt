package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CoursesPutRequestInner
import java.util.UUID

@Service
@Transactional
class CourseService(
  @Autowired
  @Qualifier("JPA")
  val courseRepository: CourseRepository,
) {
  fun allCourses(): List<CourseEntity> = courseRepository.allCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository.offeringsForCourse(courseId)

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = courseRepository.courseOffering(courseId, offeringId)
  fun replaceAllCourses(courseData: List<CoursesPutRequestInner>) {
    courseData.forEach(::println)
  }
}
