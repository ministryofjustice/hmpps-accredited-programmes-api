package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "referral_view")
data class ReferralViewEntity(
  @Id
  @Column(name = "referral_id")
  var id: UUID? = null,
  val prisonNumber: String,
  val forename: String,
  val surname: String,
  val conditionalReleaseDate: LocalDate?,
  val paroleEligibilityDate: LocalDate?,
  val tariffExpiryDate: LocalDate?,
  val earliestReleaseDate: LocalDate?,
  val earliestReleaseDateType: String?,
  val nonDtoReleaseDateType: String?,
  val organisationId: String?,
  val organisationName: String?,
  @Enumerated(EnumType.STRING)
  var status: ReferralEntity.ReferralStatus = ReferralEntity.ReferralStatus.REFERRAL_STARTED,
  val referrerUsername: String?,
  val courseName: String?,
  val audience: String?,
  var submittedOn: LocalDateTime? = null,
)

@Repository
interface ReferralViewRepository : JpaRepository<ReferralViewEntity, UUID> {

  @Query(
    value = """
      SELECT r from ReferralViewEntity r
      WHERE r.organisationId = :organisationId
        AND (:status IS NULL OR r.status IN :status)
        AND (:audience IS NULL OR :audience = '' OR r.audience = :audience)
        AND (:courseName IS NULL OR :courseName = '' OR LOWER(r.courseName) LIKE LOWER(CONCAT('%', :courseName, '%')))
    """,
    nativeQuery = false,
  )
  fun getReferralsByOrganisationId(
    organisationId: String,
    pageable: Pageable,
    status: List<ReferralEntity.ReferralStatus>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralViewEntity>

  @Query(
    value = """
      SELECT r from ReferralViewEntity r
      WHERE r.referrerUsername = :username
        AND (:status IS NULL OR r.status IN :status)
        AND (:audience IS NULL OR :audience = '' OR r.audience = :audience)
        AND (:courseName IS NULL OR :courseName = '' OR LOWER(r.courseName) LIKE LOWER(CONCAT('%', :courseName, '%')))
    """,
    nativeQuery = false,
  )
  fun getReferralsByUsername(
    username: String,
    pageable: Pageable,
    status: List<ReferralEntity.ReferralStatus>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralViewEntity>
}