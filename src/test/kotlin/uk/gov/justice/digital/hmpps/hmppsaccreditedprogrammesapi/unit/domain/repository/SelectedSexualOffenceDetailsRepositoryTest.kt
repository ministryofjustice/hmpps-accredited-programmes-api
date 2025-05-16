package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.repository

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SelectedSexualOffenceDetailsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.SelectedSexualOffenceDetailsEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.SexualOffenceDetailsEntityFactory

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test-h2")
class SelectedSexualOffenceDetailsRepositoryTest {

  @Autowired
  private lateinit var entityManager: EntityManager

  @Autowired
  private lateinit var selectedSexualOffenceDetailsRepository: SelectedSexualOffenceDetailsRepository

  @Test
  fun `SelectedSexualOffenceDetailsRepository should save and retrieve SelectedSexualOffenceDetailsEntity objects`() {
    // Given
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory().produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    // Create and persist a selected sexual offence details entity
    var selectedSexualOffenceDetails = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .produce()
    selectedSexualOffenceDetails = entityManager.merge(selectedSexualOffenceDetails)

    // When
    val selectedOffenceDetails = entityManager.find(SelectedSexualOffenceDetailsEntity::class.java, selectedSexualOffenceDetails.id)
    selectedOffenceDetails shouldNotBe null
    selectedOffenceDetails.referral.id shouldBe referral.id

    // Then
    val foundDetails = selectedSexualOffenceDetailsRepository.findAllByReferralId(referral.id!!)
    foundDetails.size shouldBe 1
    foundDetails[0].id shouldBe selectedSexualOffenceDetails.id
  }

  @Test
  fun `SelectedSexualOffenceDetailsRepository should find all details for a referral`() {
    // Create and persist a referral
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory().produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    // Create and persist multiple selected sexual offence details entities
    var details1 = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .produce()
    details1 = entityManager.merge(details1)

    var details2 = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .produce()
    details2 = entityManager.merge(details2)

    // Test the findAllByReferral_Id method
    val foundDetails = selectedSexualOffenceDetailsRepository.findAllByReferralId(referral.id!!)
    foundDetails.size shouldBe 2
    foundDetails.map { it.id } shouldContainExactlyInAnyOrder listOf(details1.id, details2.id)
  }

  @Test
  fun `A referral should have access to its selected sexual offence details`() {
    // Given
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory().produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    // Create and persist multiple sexual offence details entities
    var offenceDetails1 = SexualOffenceDetailsEntityFactory().produce()
    offenceDetails1 = entityManager.merge(offenceDetails1)
    var offenceDetails2 = SexualOffenceDetailsEntityFactory().produce()
    offenceDetails2 = entityManager.merge(offenceDetails2)

    // Create and persist multiple selected sexual offence details entities
    var selectedDetails1 = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .withSexualOffenceDetails(offenceDetails1)
      .produce()
    selectedDetails1 = entityManager.merge(selectedDetails1)

    // When
    var selectedDetails2 = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .withSexualOffenceDetails(offenceDetails2)
      .produce()
    selectedDetails2 = entityManager.merge(selectedDetails2)
    entityManager.flush()
    entityManager.clear()

    // Then
    val referralEntity = entityManager.find(ReferralEntity::class.java, referral.id)
    referralEntity.selectedSexualOffenceDetails.size shouldBe 2
    referralEntity.selectedSexualOffenceDetails.map { it.id } shouldContainExactlyInAnyOrder listOf(selectedDetails1.id, selectedDetails2.id)
  }

  @Test
  fun `SelectedSexualOffenceDetailsEntity should reference SexualOffenceDetailsEntity`() {
    // Create and persist a referral
    var course = CourseEntityFactory().produce()
    course = entityManager.merge(course)

    var offering = OfferingEntityFactory().produce()
    offering.course = course
    offering = entityManager.merge(offering)

    var referrer = ReferrerUserEntityFactory().produce()
    referrer = entityManager.merge(referrer)

    var referral = ReferralEntityFactory()
      .withId(null)
      .withReferrer(referrer)
      .withOffering(offering)
      .produce()
    referral = entityManager.merge(referral)

    // Create and persist a sexual offence details entity
    var sexualOffenceDetails = SexualOffenceDetailsEntityFactory().produce()
    sexualOffenceDetails = entityManager.merge(sexualOffenceDetails)

    // Create and persist a selected sexual offence details entity with a reference to the sexual offence details
    var selectedSexualOffenceDetails = SelectedSexualOffenceDetailsEntityFactory()
      .withId(null)
      .withReferral(referral)
      .withSexualOffenceDetails(sexualOffenceDetails)
      .produce()
    selectedSexualOffenceDetails = entityManager.merge(selectedSexualOffenceDetails)

    // Verify the entity was persisted correctly with the reference
    val persistedDetails = entityManager.find(SelectedSexualOffenceDetailsEntity::class.java, selectedSexualOffenceDetails.id)
    persistedDetails shouldNotBe null
    persistedDetails.run {
      this.referral.id shouldBe referral.id
      this.sexualOffenceDetails shouldNotBe null
      this.sexualOffenceDetails?.id shouldBe sexualOffenceDetails.id
      this.sexualOffenceDetails?.category shouldBe sexualOffenceDetails.category
      this.sexualOffenceDetails?.description shouldBe sexualOffenceDetails.description
      this.sexualOffenceDetails?.score shouldBe sexualOffenceDetails.score
    }

    // Test the findAllBySexualOffenceDetails method
    val foundDetailsByEntity = selectedSexualOffenceDetailsRepository.findAllBySexualOffenceDetails(sexualOffenceDetails)
    foundDetailsByEntity.size shouldBe 1
    foundDetailsByEntity[0].id shouldBe selectedSexualOffenceDetails.id

    // Test the findAllBySexualOffenceDetails_Id method
    val foundDetailsById = selectedSexualOffenceDetailsRepository.findAllBySexualOffenceDetailsId(sexualOffenceDetails.id!!)
    foundDetailsById.size shouldBe 1
    foundDetailsById[0].id shouldBe selectedSexualOffenceDetails.id
  }
}
