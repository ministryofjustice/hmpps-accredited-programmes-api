package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.OffenderAllocationResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.PomDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.Account
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetailResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import java.math.BigInteger

@ExtendWith(MockKExtension::class)
class StaffServiceTest {

  private val allocationManagerService: AllocationManagerService = mockk()
  private val nomisUserRolesService: NomisUserRolesService = mockk()
  private val staffRepository: StaffRepository = mockk()

  private val service = StaffService(allocationManagerService, nomisUserRolesService, staffRepository)

  @Test
  fun `getOffenderAllocation should return primary and secondary POM details`() {
    val prisonNumber = "A1234BC"
    val offenderAllocation = OffenderAllocationResponse(
      primaryPom = PomDetail(1, "John"),
      secondaryPom = PomDetail(2, "Jane"),
    )

    val primaryPomDetail = StaffDetailResponse(
      staffId = BigInteger("1"),
      firstName = "John",
      lastName = "Doe",
      status = "ACTIVE",
      primaryEmail = "john.doe@example.com",
      generalAccount = Account("jdoe"),
      adminAccount = null,
    )
    val secondaryPomDetail = StaffDetailResponse(
      staffId = BigInteger("2"),
      firstName = "Jane",
      lastName = "Smith",
      status = "ACTIVE",
      primaryEmail = "jane.smith@example.com",
      generalAccount = Account("jsmith"),
      adminAccount = null,
    )

    every { allocationManagerService.getOffenderAllocation(prisonNumber) } returns offenderAllocation
    every { nomisUserRolesService.getStaffDetail("1") } returns primaryPomDetail
    every { nomisUserRolesService.getStaffDetail("2") } returns secondaryPomDetail

    val result = service.getOffenderAllocation(prisonNumber)

    assertEquals(primaryPomDetail, result.first)
    assertEquals(secondaryPomDetail, result.second)

    verify {
      allocationManagerService.getOffenderAllocation(prisonNumber)
      nomisUserRolesService.getStaffDetail("1")
      nomisUserRolesService.getStaffDetail("2")
    }
  }

  @Test
  fun `getOffenderAllocation should throw exception if offender allocation is null`() {
    val prisonNumber = "A1234BC"
    every { allocationManagerService.getOffenderAllocation(prisonNumber) } returns null

    val exception = assertThrows<BusinessException> {
      service.getOffenderAllocation(prisonNumber)
    }
    assertEquals("No POM details found for $prisonNumber", exception.message)

    verify { allocationManagerService.getOffenderAllocation(prisonNumber) }
  }

  @Test
  fun `buildStaffEntity should build correct StaffEntity`() {
    val staffDetailResponse = StaffDetailResponse(
      staffId = BigInteger("1"),
      firstName = "John",
      lastName = "Doe",
      status = "ACTIVE",
      primaryEmail = "john.doe@example.com",
      generalAccount = Account("jdoe"),
      adminAccount = null,
    )

    val result = service.buildStaffEntity(staffDetailResponse)

    assertEquals("1".toBigInteger(), result.staffId)
    assertEquals("John", result.firstName)
    assertEquals("Doe", result.lastName)
    assertEquals("john.doe@example.com", result.primaryEmail)
    assertEquals("jdoe", result.username)
    assertEquals(AccountType.GENERAL, result.accountType)
  }
}
