package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import java.util.UUID

@Entity
@Table(name = "referral_status")
data class ReferralStatusEntity(
  @Id
  val code: String,
  val description: String,
  val hintText: String,
  val colour: String,
  val hasNotes: Boolean,
  val hasConfirmation: Boolean,
  val confirmationText: String,
  val active: Boolean,
  val draft: Boolean,
  val closed: Boolean,
  val hold: Boolean,
  val release: Boolean,
  val defaultOrder: Int,
  val notesOptional: Boolean,
  val caseNotesSubtype: String,
  val caseNotesMessage: String,
)

@Entity
@Table(name = "referral_status_category")
data class ReferralStatusCategoryEntity(
  @Id
  val code: String,
  val description: String,
  val referralStatusCode: String,
  val active: Boolean,
)

@Entity
@Table(name = "referral_status_reason")
data class ReferralStatusReasonEntity(
  @Id
  val code: String,
  val description: String,
  val referralStatusCategoryCode: String,
  val active: Boolean,
  val deselectOpen: Boolean,
)

@Repository
interface ReferralStatusRepository : JpaRepository<ReferralStatusEntity, UUID> {
  fun findByCode(code: String): ReferralStatusEntity?
  fun findAllByActiveIsTrueOrderByDefaultOrder(): List<ReferralStatusEntity>

  // get draft statuses only
  fun findAllByActiveIsTrueAndDraftIsTrueOrderByDefaultOrder(): List<ReferralStatusEntity>

  // get closed statuses only
  fun findAllByActiveIsTrueAndClosedIsTrueOrderByDefaultOrder(): List<ReferralStatusEntity>

  // get open statuses only
  fun findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalseOrderByDefaultOrder(): List<ReferralStatusEntity>
}

fun ReferralStatusRepository.getByCode(code: String) =
  findByCode(code) ?: throw NotFoundException("No Referral status found with id=$code")

@Repository
interface ReferralStatusCategoryRepository : JpaRepository<ReferralStatusCategoryEntity, UUID> {
  fun getAllByReferralStatusCodeAndActiveIsTrue(statusCode: String): List<ReferralStatusCategoryEntity>
  fun findByCode(code: String): ReferralStatusCategoryEntity?
}

fun ReferralStatusCategoryRepository.getByCode(code: String) =
  findByCode(code) ?: throw NotFoundException("No Referral status category found with id=$code")

@Repository
interface ReferralStatusReasonRepository : JpaRepository<ReferralStatusReasonEntity, UUID> {

  @Query(
    value = """
      SELECT e
      FROM ReferralStatusReasonEntity e
      WHERE e.referralStatusCategoryCode = :statusCode
      AND e.active is TRUE
      AND (:deselectOpen = FALSE OR e.deselectOpen = TRUE)
    """,
  )
  fun getAllByReferralStatusCategoryCodeAndActiveIsTrue(
    statusCode: String,
    deselectOpen: Boolean,
  ): List<ReferralStatusReasonEntity>

  fun findByCode(code: String): ReferralStatusReasonEntity?
}

fun ReferralStatusReasonRepository.getByCode(code: String) =
  findByCode(code) ?: throw NotFoundException("No Referral status reason found with id=$code")
