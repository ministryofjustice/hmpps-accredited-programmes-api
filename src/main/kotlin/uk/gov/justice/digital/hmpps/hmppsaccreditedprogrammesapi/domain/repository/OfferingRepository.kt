package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.GenderOffering
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.math.BigInteger
import java.time.LocalDate
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<OfferingEntity, UUID> {
  fun findAllByCourseId(courseId: UUID): List<OfferingEntity>
  fun findAllByCourseIdAndWithdrawnIsFalse(courseId: UUID): List<OfferingEntity>
  fun findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId: UUID, organisationId: String): OfferingEntity?
  fun findByCourseIdAndIdAndWithdrawnIsFalse(courseId: UUID, offeringId: UUID): OfferingEntity?


  @Query(
    """
       SELECT
         COUNT(*) AS count,
         rsh.status AS status,
         o.organisation_id AS orgId
       FROM referral r
       JOIN offering o ON r.offering_id = o.offering_id
       JOIN course c ON o.course_id = c.course_id
       JOIN referral_status_history rsh ON rsh.referral_id = r.referral_id
       WHERE r.deleted = FALSE
         AND rsh.status_start_date >= :startDate
         AND rsh.status_start_date < :endDate
         AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
       GROUP BY rsh.status, o.organisation_id
       ORDER BY o.organisation_id, rsh.status
          """,
    nativeQuery = true,
  )
  fun findAllByCourseId_(courseId: UUID): List<GenderOfferingProjection>

  @Modifying
  @Query("delete from OfferingEntity o where o.id = :offeringId")
  fun delete(offeringId: UUID)
}

interface GenderOfferingProjection {
  fun getCount(): BigInteger
  fun getStatus(): String
  fun getOrgId(): String
}