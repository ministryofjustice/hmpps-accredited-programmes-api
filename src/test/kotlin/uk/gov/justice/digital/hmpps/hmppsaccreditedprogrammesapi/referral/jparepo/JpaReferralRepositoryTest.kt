package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.jparepo

import io.kotest.matchers.optional.shouldBePresent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.CourseRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain.Offering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.jpa.RepositoryTest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.shared.jpa.commitAndStartNewTx
import java.util.UUID

class JpaReferralRepositoryTest
@Autowired
constructor(
  val repository: JpaReferralRepository,
  val courseRepository: CourseRepository,
  jdbcTemplate: JdbcTemplate,
) : RepositoryTest(jdbcTemplate) {
  @Test
  fun `save and retrieve a referral`() {
    val persistentOfferingId = persistOffering()
    val referralId = repository.save(Referral(referrerId = "refId", prisonNumber = "A1234AA", offeringId = persistentOfferingId)).id!!

    commitAndStartNewTx()

    repository.findById(referralId) shouldBePresent {
      referrerId shouldBe "refId"
      prisonNumber shouldBe "A1234AA"
      offeringId shouldBe persistentOfferingId
    }
  }

  private fun persistOffering(): UUID {
    courseRepository.saveCourse(
      CourseEntity(
        identifier = "C",
        name = "Course",
        alternateName = "Alt C",
      ).apply {
        addOffering(Offering(organisationId = "MDI", contactEmail = "a@b.c"))
      },
    )
    commitAndStartNewTx()
    return courseRepository.allCourses()[0].offerings.first().id!!
  }
}
