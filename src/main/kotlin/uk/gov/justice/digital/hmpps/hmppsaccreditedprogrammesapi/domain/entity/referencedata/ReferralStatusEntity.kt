package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import java.util.UUID

@Entity
@Table(name = "referral_status")
data class ReferralStatusEntity(
  @Id
  val code: String,
  val description: String,
  val colour: String,
  val active: Boolean,
  val draft: Boolean,
  val closed: Boolean,
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
)

@Repository
interface ReferralStatusRepository : JpaRepository<ReferralStatusEntity, UUID> {
  fun findByCode(code: String): ReferralStatusEntity?
  fun findAllByActiveIsTrue(): List<ReferralStatusEntity>

  // get draft statuses only
  fun findAllByActiveIsTrueAndDraftIsTrue(): List<ReferralStatusEntity>

  // get closed statuses only
  fun findAllByActiveIsTrueAndClosedIsTrue(): List<ReferralStatusEntity>

  // get open statuses only
  fun findAllByActiveIsTrueAndClosedIsFalseAndDraftIsFalse(): List<ReferralStatusEntity>
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
  fun getAllByReferralStatusCategoryCodeAndActiveIsTrue(statusCode: String): List<ReferralStatusReasonEntity>
  fun findByCode(code: String): ReferralStatusReasonEntity?
}

fun ReferralStatusReasonRepository.getByCode(code: String) =
  findByCode(code) ?: throw NotFoundException("No Referral status reason found with id=$code")
