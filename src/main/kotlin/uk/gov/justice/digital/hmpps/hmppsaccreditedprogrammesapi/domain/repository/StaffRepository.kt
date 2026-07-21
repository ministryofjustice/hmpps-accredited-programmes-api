package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import java.math.BigInteger
import java.util.UUID

/**
 * Projection returned by [StaffRepository.findSurnamesByUsernames] so callers can
 * build a `username -> lastName` map without loading full staff rows.
 */
interface UsernameSurnameProjection {
  val username: String
  val lastName: String
}

/**
 * Projection returned by [StaffRepository.findSurnamesByStaffIds] so callers can
 * build a `staffId -> lastName` map without loading full staff rows.
 */
interface StaffIdSurnameProjection {
  val staffId: BigInteger
  val lastName: String
}

@Repository
interface StaffRepository : JpaRepository<StaffEntity, UUID> {

  fun findByStaffId(staffId: BigInteger): StaffEntity?

  @Query("SELECT s.lastName FROM StaffEntity s WHERE s.username = :username")
  fun findLastNameByUsername(username: String): List<String>

  @Query("SELECT s.lastName FROM StaffEntity s WHERE s.staffId = :staffId")
  fun findLastNameByStaffId(staffId: BigInteger): List<String>

  /**
   * Batch equivalent of [findLastNameByUsername]: returns one row per matched
   * staff record so callers can group into a `username -> lastName` map in a
   * single query. Uses idx_staff_username (see V144).
   */
  @Query(
    """
    SELECT s.username AS username, s.lastName AS lastName
    FROM StaffEntity s
    WHERE s.username IN :usernames
    """,
  )
  fun findSurnamesByUsernames(usernames: Collection<String>): List<UsernameSurnameProjection>

  /**
   * Batch equivalent of [findLastNameByStaffId]: returns one row per matched
   * staff record so callers can group into a `staffId -> lastName` map in a
   * single query. Uses idx_staff_staff_id (see V144).
   */
  @Query(
    """
    SELECT s.staffId AS staffId, s.lastName AS lastName
    FROM StaffEntity s
    WHERE s.staffId IN :staffIds
    """,
  )
  fun findSurnamesByStaffIds(staffIds: Collection<BigInteger>): List<StaffIdSurnameProjection>

  @Query(
    """
    SELECT DISTINCT s FROM StaffEntity s 
    JOIN ReferralEntity r ON s.staffId = r.primaryPomStaffId OR s.staffId = r.secondaryPomStaffId 
    WHERE r.prisonNumber = :prisonNumber
  """,
  )
  fun findByPrisonNumber(prisonNumber: String): List<StaffEntity>
}
