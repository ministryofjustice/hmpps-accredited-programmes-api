package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.SelectedSexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import java.util.UUID

@Repository
interface SelectedSexualOffenceDetailsRepository : JpaRepository<SelectedSexualOffenceDetailsEntity, UUID> {
  fun findAllByReferralId(referralId: UUID): List<SelectedSexualOffenceDetailsEntity>
  fun findAllBySexualOffenceDetails(sexualOffenceDetails: SexualOffenceDetailsEntity): List<SelectedSexualOffenceDetailsEntity>
  fun findAllBySexualOffenceDetailsId(sexualOffenceDetailsId: UUID): List<SelectedSexualOffenceDetailsEntity>
}
