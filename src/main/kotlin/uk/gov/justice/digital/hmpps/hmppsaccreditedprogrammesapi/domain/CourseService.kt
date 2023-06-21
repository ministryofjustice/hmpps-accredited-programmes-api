package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.CourseRecord
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PrerequisiteRecord
import java.util.UUID

@Service
@Transactional
class CourseService(
  @Autowired
  val courseRepository: MutableCourseRepository,
) {
  fun allCourses(): List<CourseEntity> = courseRepository.allCourses()

  fun course(courseId: UUID): CourseEntity? = courseRepository.course(courseId)

  fun offeringsForCourse(courseId: UUID): List<Offering> = courseRepository.offeringsForCourse(courseId)

  fun courseOffering(courseId: UUID, offeringId: UUID): Offering? = courseRepository.courseOffering(courseId, offeringId)

  fun replaceAllCourses(courseData: List<CourseRecord>) {
    courseRepository.clear()
    courseRepository.saveAudiences(courseData.flatMap { audienceStrings(it.audience) }.map(::Audience).toSet())

    val allAudiences: Map<String, Audience> = courseRepository.allAudiences().associateBy { it.value }

    courseData.map {
      CourseEntity(
        name = it.name,
        description = it.description,
        audiences = audienceStrings(it.audience).mapNotNull { audienceName -> allAudiences[audienceName] }.toMutableSet(),
      )
    }.forEach(courseRepository::saveCourse)
  }

  private fun audienceStrings(audience: String): List<String> = audience.split(',').map(String::trim)

  fun replaceAllPrerequisites(replacements: List<PrerequisiteRecord>) {
    val allCourses = courseRepository.allCourses()
    clearPrerequisites(allCourses)
    val coursesByName = allCourses.associateBy(CourseEntity::name)
    replacements.forEach { record ->
      coursesByName[record.course]?.run {
        prerequisites.add(Prerequisite(name = record.name, description = record.description ?: ""))
      }
    }
  }

  private fun clearPrerequisites(courses: List<CourseEntity>) {
    courses.forEach { it.prerequisites.clear() }
  }
}
