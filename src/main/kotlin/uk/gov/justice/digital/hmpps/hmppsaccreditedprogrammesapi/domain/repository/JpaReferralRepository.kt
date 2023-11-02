package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.util.UUID

@Repository
interface JpaReferralRepository : JpaRepository<ReferralEntity, UUID> {

  @Query
  ("SELECT r.id, r.status, r.prisonNumber, r.referrerId, r.offeringId from ReferralEntity r INNER JOIN OfferingEntity o ON o.id = r.offeringId where o.organisationId = :orgId")
  fun getReferralsByOrgId(orgId: String): List<ReferralEntity>
}
