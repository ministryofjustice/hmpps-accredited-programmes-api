package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PomType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository

@Service
@Transactional
class PrisonOffenderManagerService(
  private val allocationManagerService: AllocationManagerService,
  private val nomisUserRolesService: NomisUserRolesService,
  private val staffRepository: StaffRepository,
) {

  fun getOffenderAllocation(prisonNumber: String): Pair<StaffDetail?, StaffDetail?> {
    val offenderAllocation = allocationManagerService.getOffenderAllocation(prisonNumber)

    offenderAllocation?.let {
      val primaryPom = nomisUserRolesService.getStaffDetail(it.primaryPrisonOffenderManager.staffId.toString())
      val secondaryPom = nomisUserRolesService.getStaffDetail(it.secondaryPrisonOffenderManager.staffId.toString())

      return Pair(primaryPom, secondaryPom)
    } ?: throw BusinessException("No POM details found for $prisonNumber")
  }

  fun savePrisonOffenderManagers(
    submittedReferral: ReferralEntity,
    offenderAllocation: Pair<StaffDetail?, StaffDetail?>,
  ) {
    val primaryPom = buildStaffEntity(offenderAllocation.first, PomType.PRIMARY, submittedReferral)
    val secondaryPom = buildStaffEntity(offenderAllocation.second, PomType.SECONDARY, submittedReferral)

    staffRepository.saveAll(listOf(primaryPom, secondaryPom))
  }

  private fun buildStaffEntity(staffDetail: StaffDetail?, pomType: PomType, referralEntity: ReferralEntity): StaffEntity {
    return StaffEntity(
      staffId = staffDetail?.staffId,
      firstName = staffDetail?.firstName.orEmpty(),
      lastName = staffDetail?.lastName.orEmpty(),
      primaryEmail = staffDetail?.primaryEmail.orEmpty(),
      username = staffDetail?.generalAccount?.username ?: staffDetail?.adminAccount?.username.orEmpty(),
      pomType = pomType,
      accountType = staffDetail?.generalAccount?.let { AccountType.GENERAL } ?: AccountType.ADMIN,
      referral = referralEntity,
    )
  }
}
