package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OfferingEntity
import java.util.UUID

@Repository
interface OfferingRepository : JpaRepository<OfferingEntity, UUID> {
  fun findAllByCourseId(courseId: UUID): List<OfferingEntity>
  fun findAllByCourseIdAndWithdrawnIsFalse(courseId: UUID): List<OfferingEntity>
  fun findByCourseIdAndOrganisationIdAndWithdrawnIsFalse(courseId: UUID, organisationId: String): OfferingEntity?
  fun findByCourseIdAndOrganisationId(courseId: UUID, organisationId: String): OfferingEntity?
  fun findByCourseIdAndIdAndWithdrawnIsFalse(courseId: UUID, offeringId: UUID): OfferingEntity?
  fun findByOrganisationId(organisationId: String): List<OfferingEntity>

  @Modifying
  @Query("delete from OfferingEntity o where o.id = :offeringId")
  fun delete(offeringId: UUID)

  @Query(
    """
    SELECT o FROM OfferingEntity o
    LEFT JOIN ReferralEntity r ON r.offering.id = o.id
    WHERE o.organisationId = :organisationId
    GROUP BY o.id
    HAVING o.withdrawn = false OR COUNT(r.id) > 0
  """,
  )
  fun findOfferingsByOrganisationIdWithActiveReferrals(organisationId: String): List<OfferingEntity>
}
