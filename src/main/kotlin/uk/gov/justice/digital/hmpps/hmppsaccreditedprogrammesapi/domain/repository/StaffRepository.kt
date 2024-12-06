package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import java.math.BigInteger
import java.util.UUID

@Repository
interface StaffRepository : JpaRepository<StaffEntity, UUID> {

  fun findByReferralId(referralId: UUID): StaffEntity?

  fun findByStaffId(staffId: BigInteger): StaffEntity?
}
