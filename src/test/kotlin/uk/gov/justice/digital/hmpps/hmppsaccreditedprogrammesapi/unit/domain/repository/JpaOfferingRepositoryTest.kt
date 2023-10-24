package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaCourseEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.JpaOfferingRepository
import kotlin.jvm.optionals.getOrNull

class JpaOfferingRepositoryTest
@Autowired
constructor(
  val courseEntityRepository: JpaCourseEntityRepository,
  val offeringRepository: JpaOfferingRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTestBase(jdbcTemplate) {
  @Test
  fun `JpaOfferingRepository should retrieve the correct offering for a CourseEntity object given a valid offeringId`() {
    val course1 = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A description",
      referable = true,
    ).apply {
      addOffering(OfferingEntity(organisationId = "BWI", contactEmail = "bwi@a.com"))
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
      addOffering(OfferingEntity(organisationId = "BXI", contactEmail = "bxi@a.com"))
    }

    val course2 = CourseEntity(
      name = "Another Course",
      identifier = "ACANO",
      description = "Another description",
      referable = false,
    ).apply {
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
    }

    val offering = courseEntityRepository.save(course1).offerings.first()
    courseEntityRepository.save(course2)

    commitAndStartNewTx()

    val persistentOffering = offeringRepository.findById(offering.id!!).getOrNull()

    persistentOffering.shouldNotBeNull()
    persistentOffering shouldBeEqualToComparingFields offering
  }

  @Test
  fun `JpaOfferingepository should retrieve the correct offering for a CourseEntity object given a valid withdrawn offeringId`() {
    val course1 = CourseEntity(
      name = "A Course",
      identifier = "AC",
      description = "A description",
      referable = true,
    ).apply {
      addOffering(OfferingEntity(organisationId = "BWI", contactEmail = "bwi@a.com", withdrawn = true))
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com", withdrawn = true))
      addOffering(OfferingEntity(organisationId = "BXI", contactEmail = "bxi@a.com", withdrawn = true))
    }

    val course2 = CourseEntity(
      name = "Another Course",
      identifier = "ACANO",
      description = "Another description",
      referable = false,
    ).apply {
      addOffering(OfferingEntity(organisationId = "MDI", contactEmail = "mdi@a.com"))
    }

    val offering = courseEntityRepository.save(course1).offerings.first()
    courseEntityRepository.save(course2)

    commitAndStartNewTx()

    val persistentOffering = offeringRepository.findById(offering.id!!).getOrNull()

    persistentOffering.shouldNotBeNull()
    persistentOffering shouldBeEqualToComparingFields offering
  }
}