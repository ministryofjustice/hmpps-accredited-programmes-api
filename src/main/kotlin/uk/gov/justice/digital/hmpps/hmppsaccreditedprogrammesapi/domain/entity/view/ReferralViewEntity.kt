package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
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
  var status: String?,
  var statusDescription: String?,
  var statusColour: String?,
  val referrerUsername: String?,
  val courseName: String?,
  val audience: String?,
  var submittedOn: LocalDateTime? = null,
  val sentenceType: String?,
  val listDisplayName: String?,
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
        AND (:prisonNumber IS NULL OR LOWER(cast (r.prisonNumber as string)) = LOWER(:prisonNumber))
        AND (:surnameOnly IS NULL OR (LOWER(r.surname) LIKE LOWER(CONCAT('%', :surnameOnly, '%')) 
                                  OR LOWER(r.forename) LIKE LOWER(CONCAT('%', :surnameOnly, '%'))))
        AND (:forename IS NULL OR LOWER(r.forename) LIKE LOWER(CONCAT('%', :forename, '%')))
        AND (:surname IS NULL OR LOWER(r.surname) LIKE LOWER(CONCAT('%', :surname, '%')))
    """,
  )
  fun getReferralsByOrganisationId(
    organisationId: String,
    prisonNumber: String?,
    surnameOnly: String?,
    forename: String?,
    surname: String?,
    pageable: Pageable,
    status: List<String>?,
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
        AND (:prisonNumber IS NULL OR LOWER(cast (r.prisonNumber as string)) = LOWER(:prisonNumber))
        AND (:surnameOnly IS NULL OR (LOWER(r.surname) LIKE LOWER(CONCAT('%', :surnameOnly, '%')) 
                                  OR LOWER(r.forename) LIKE LOWER(CONCAT('%', :surnameOnly, '%'))))
        AND (:forename IS NULL OR LOWER(r.forename) LIKE LOWER(CONCAT('%', :forename, '%')))
        AND (:surname IS NULL OR LOWER(r.surname) LIKE LOWER(CONCAT('%', :surname, '%')))
    """,
  )
  fun getReferralsByUsername(
    prisonNumber: String?,
    surnameOnly: String?,
    forename: String?,
    surname: String?,
    username: String,
    pageable: Pageable,
    status: List<String>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralViewEntity>
}
