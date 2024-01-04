package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CourseService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory

class OfferingRepositoryTest
@Autowired
constructor(
  val courseRepository: CourseRepository,
  val offeringRepository: OfferingRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {

  @Autowired
  private lateinit var courseService: CourseService

  @ParameterizedTest
  @ValueSource(booleans = [true, false])
  fun `JpaOfferingRepository should retrieve the correct offering for a CourseEntity object given a valid offeringId`(isWithdrawn: Boolean) {
    val course = CourseEntityFactory().produce()
    courseRepository.save(course)

    val offering = OfferingEntityFactory().withWithdrawn(isWithdrawn).produce()
    courseService.addOfferingToCourse(course.id!!, offering)

    val persistedOfferings = offeringRepository.findAllByCourseId(course.id!!)
    val persistedCourse = courseRepository.findByMutableOfferingsId(offering.id!!)

    persistedOfferings.first().withdrawn shouldBe isWithdrawn
    persistedCourse?.id shouldBe course.id
  }

  @Test
  fun `Given an offering that is subsequently replaced by another, findAllByCourseId should return both the new and withdrawn offerings`() {
    val course = CourseEntityFactory().produce()
    courseRepository.save(course)

    val offeringWithdrawnFalse = OfferingEntityFactory().withWithdrawn(false).produce()
    courseService.addOfferingToCourse(course.id!!, offeringWithdrawnFalse)


    val offeringWithdrawnTrue = OfferingEntityFactory().withWithdrawn(true).produce()
    courseService.addOfferingToCourse(course.id!!, offeringWithdrawnTrue)

    val persistedOfferings = offeringRepository.findAllByCourseId(course.id!!)
    val persistedCourseForOfferingWithdrawnFalse = courseRepository.findByMutableOfferingsId(offeringWithdrawnFalse.id!!)
    val persistedCourseForOfferingWithdrawnTrue = courseRepository.findByMutableOfferingsId(offeringWithdrawnTrue.id!!)

    persistedOfferings shouldContainExactlyInAnyOrder listOf(offeringWithdrawnFalse, offeringWithdrawnTrue)
    persistedCourseForOfferingWithdrawnFalse shouldBe persistedCourseForOfferingWithdrawnTrue
  }
}
