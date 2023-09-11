package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.jparepo

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.commitAndStartNewTx
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import kotlin.jvm.optionals.getOrNull

class OfferingRepositoryTest
@Autowired
constructor(
  val courseRepository: CourseEntityRepository,
  val offeringRepository: OfferingRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `find offering by id`() {
    val course1 = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A description",
      referable = true,
    ).apply {
      addOffering(Offering(organisationId = "BWI", contactEmail = "bwi@a.com"))
      addOffering(Offering(organisationId = "MDI", contactEmail = "mdi@a.com"))
      addOffering(Offering(organisationId = "BXI", contactEmail = "bxi@a.com"))
    }

    val course2 = CourseEntity(
      name = "Another Course",
      identifier = "ACANO",
      description = "Another description",
      referable = false,
    ).apply {
      addOffering(Offering(organisationId = "MDI", contactEmail = "mdi@a.com"))
    }

    val offering = courseRepository.save(course1).offerings.first()
    courseRepository.save(course2)

    commitAndStartNewTx()

    val persistentOffering = offeringRepository.findById(offering.id!!).getOrNull()

    persistentOffering.shouldNotBeNull()
    persistentOffering shouldBeEqualToComparingFields offering
  }
}
