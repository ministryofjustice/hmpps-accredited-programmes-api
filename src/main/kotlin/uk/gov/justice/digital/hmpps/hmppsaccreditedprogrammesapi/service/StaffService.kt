package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import java.math.BigInteger

@Service
class StaffService(
  private val allocationManagerService: AllocationManagerService,
  private val nomisUserRolesService: NomisUserRolesService,
  private val staffRepository: StaffRepository,
) {

  fun getOffenderAllocation(prisonNumber: String): Pair<StaffDetail?, StaffDetail?> {
    val offenderAllocation = allocationManagerService.getOffenderAllocation(prisonNumber)

    offenderAllocation?.let {
      val primaryPom = nomisUserRolesService.getStaffDetail(it.primaryPom?.staffId.toString())
      val secondaryPom = nomisUserRolesService.getStaffDetail(it.secondaryPom?.staffId.toString())

      return Pair(primaryPom, secondaryPom)
    } ?: throw BusinessException("No POM details found for $prisonNumber")
  }

  fun saveStaffIfNotPresent(staffDetail: StaffDetail?) =
    staffDetail?.staffId?.let {
      staffRepository.findByStaffId(it)
        ?: return staffRepository.save(buildStaffEntity(staffDetail))
    }

  fun getStaffDetail(staffId: BigInteger): StaffEntity? = staffRepository.findByStaffId(staffId)

  fun buildStaffEntity(staffDetail: StaffDetail?): StaffEntity {
    return StaffEntity(
      staffId = staffDetail?.staffId,
      firstName = staffDetail?.firstName.orEmpty(),
      lastName = staffDetail?.lastName.orEmpty(),
      primaryEmail = staffDetail?.primaryEmail.orEmpty(),
      username = staffDetail?.generalAccount?.username ?: staffDetail?.adminAccount?.username.orEmpty(),
      accountType = staffDetail?.generalAccount?.let { AccountType.GENERAL } ?: AccountType.ADMIN,
    )
  }
}
