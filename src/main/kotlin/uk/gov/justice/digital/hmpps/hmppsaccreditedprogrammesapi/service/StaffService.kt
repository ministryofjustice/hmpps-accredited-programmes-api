package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetailResponse
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

  fun getOffenderAllocation(prisonNumber: String): Pair<StaffDetailResponse?, StaffDetailResponse?> {
    val offenderAllocation = allocationManagerService.getOffenderAllocation(prisonNumber)

    offenderAllocation?.let {
      val primaryPom = nomisUserRolesService.getStaffDetail(it.primaryPom?.staffId.toString())
      val secondaryPom = nomisUserRolesService.getStaffDetail(it.secondaryPom?.staffId.toString())

      return Pair(primaryPom, secondaryPom)
    } ?: throw BusinessException("No POM details found for $prisonNumber")
  }

  fun saveStaffIfNotPresent(staffDetailResponse: StaffDetailResponse?) =
    staffDetailResponse?.staffId?.let {
      staffRepository.findByStaffId(it)
        ?: return staffRepository.save(buildStaffEntity(staffDetailResponse))
    }

  fun getStaffDetail(staffId: BigInteger?): StaffEntity? = staffId?.let { staffRepository.findByStaffId(it) }

  fun buildStaffEntity(staffDetailResponse: StaffDetailResponse?): StaffEntity {
    return StaffEntity(
      staffId = staffDetailResponse?.staffId,
      firstName = staffDetailResponse?.firstName.orEmpty(),
      lastName = staffDetailResponse?.lastName.orEmpty(),
      primaryEmail = staffDetailResponse?.primaryEmail.orEmpty(),
      username = staffDetailResponse?.generalAccount?.username ?: staffDetailResponse?.adminAccount?.username.orEmpty(),
      accountType = staffDetailResponse?.generalAccount?.let { AccountType.GENERAL } ?: AccountType.ADMIN,
    )
  }
}
