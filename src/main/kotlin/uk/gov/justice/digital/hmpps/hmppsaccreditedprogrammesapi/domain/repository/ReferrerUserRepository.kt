package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity

@Repository
interface ReferrerUserRepository : JpaRepository<ReferrerUserEntity, String> {

  /**
   * Returns the subset of the supplied [usernames] that have a matching
   * `referrer_user` row. Used by the SAR read layer to tell an
   * auto-derived `CourseParticipationEntity.source` (a copy of
   * `referral.referrer.username`) apart from a free-text UI value such
   * as `"OASys"`, so referrer usernames can be replaced with
   * `No Data Held` on the rendered report instead of leaking the raw
   * NOMIS username.
   *
   * Callers must guard against passing an empty collection.
   */
  @Query("SELECT r.username FROM ReferrerUserEntity r WHERE r.username IN :usernames")
  fun findExistingUsernamesIn(@Param("usernames") usernames: Collection<String>): List<String>
}
