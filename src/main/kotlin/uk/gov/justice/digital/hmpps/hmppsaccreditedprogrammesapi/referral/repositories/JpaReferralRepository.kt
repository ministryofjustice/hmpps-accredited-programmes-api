package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral
import java.util.UUID

@Repository
interface JpaReferralRepository : JpaRepository<Referral, UUID>