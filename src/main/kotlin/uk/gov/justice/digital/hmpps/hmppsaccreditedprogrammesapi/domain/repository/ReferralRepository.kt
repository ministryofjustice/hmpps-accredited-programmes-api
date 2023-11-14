package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.util.UUID

@Repository
interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {
  @Query(
    value = "SELECT r.* FROM referral r INNER JOIN offering o ON o.offering_id = r.offering_id WHERE o.organisation_id = :organisationId",
    countQuery = "SELECT count(*) FROM referral r INNER JOIN offering o ON o.offering_id = r.offering_id WHERE o.organisation_id = :organisationId",
    nativeQuery = true,
  )
  fun getReferralsByOrganisationId(organisationId: String, pageable: Pageable): Page<ReferralEntity>
}