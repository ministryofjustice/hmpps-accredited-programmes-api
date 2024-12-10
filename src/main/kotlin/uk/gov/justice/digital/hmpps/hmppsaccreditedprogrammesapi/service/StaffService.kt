package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetailResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import java.math.BigInteger

@Service
@Transactional
class StaffService(
  private val allocationManagerService: AllocationManagerService,
  private val nomisUserRolesService: NomisUserRolesService,
  private val staffRepository: StaffRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getOffenderAllocation(prisonNumber: String): Pair<StaffEntity?, StaffEntity?> {
    val offenderAllocation = allocationManagerService.getOffenderAllocation(prisonNumber)

    log.info("Offender allocation for $prisonNumber: Primary Pom staffId ${offenderAllocation?.primaryPom?.staffId}")
    log.info("Offender allocation for $prisonNumber: Secondary Pom staffId ${offenderAllocation?.secondaryPom?.staffId}")
    offenderAllocation?.let {
      return Pair(
        fetchPomDetailsIfNotAlreadyExists(it.primaryPom?.staffId, prisonNumber, PomType.PRIMARY),
        fetchPomDetailsIfNotAlreadyExists(it.secondaryPom?.staffId, prisonNumber, PomType.SECONDARY),
      )
    } ?: throw BusinessException("No POM details found for $prisonNumber")
  }

  fun fetchPomDetailsIfNotAlreadyExists(staffId: BigInteger?, prisonNumber: String, pomType: PomType): StaffEntity? {
    if (staffId == null) {
      log.warn("No $pomType pom staffId found for $prisonNumber")
      return null
    }

    val staff = staffRepository.findByStaffId(staffId)
    return if (staff == null) {
      val primaryPom = nomisUserRolesService.getStaffDetail(staffId.toString())
      staffRepository.save(buildStaffEntity(primaryPom))
    } else {
      staff
    }
  }

  fun getStaffDetail(staffId: BigInteger?): StaffEntity? = staffId?.let { staffRepository.findByStaffId(it) }

  fun buildStaffEntity(staffDetailResponse: StaffDetailResponse?): StaffEntity {
    return StaffEntity(
      staffId = staffDetailResponse?.staffId,
      firstName = staffDetailResponse?.firstName.orEmpty(),
      lastName = staffDetailResponse?.lastName.orEmpty(),
      primaryEmail = staffDetailResponse?.primaryEmail.orEmpty(),
      username = staffDetailResponse?.generalAccount?.username ?: staffDetailResponse?.adminAccount?.username.orEmpty(),
      accountType = staffDetailResponse?.generalAccount?.let { AccountType.GENERAL.name } ?: AccountType.ADMIN.name,
    )
  }
}

enum class PomType {
  PRIMARY,
  SECONDARY,
}
