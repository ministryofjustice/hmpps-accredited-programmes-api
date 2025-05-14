package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.OffenderAllocationResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.allocationManagerApi.model.PomDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.Account
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.nomisUserRoleManagementApi.model.StaffDetailResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.StaffEntityFactory

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
      primaryPom = PomDetail("1".toBigInteger(), "John"),
      secondaryPom = PomDetail("2".toBigInteger(), "Jane"),
    )

    val primaryPomDetail = StaffDetailResponse(
      staffId = "1".toBigInteger(),
      firstName = "John",
      lastName = "Doe",
      status = "ACTIVE",
      primaryEmail = "john.doe@example.com",
      generalAccount = Account("jdoe"),
      adminAccount = null,
    )
    val secondaryPomDetail = StaffDetailResponse(
      staffId = "2".toBigInteger(),
      firstName = "Jane",
      lastName = "Smith",
      status = "ACTIVE",
      primaryEmail = "jane.smith@example.com",
      generalAccount = Account("jsmith"),
      adminAccount = null,
    )

    every { allocationManagerService.getOffenderAllocation(any()) } returns offenderAllocation
    every { nomisUserRolesService.getStaffDetail("1") } returns primaryPomDetail
    every { nomisUserRolesService.getStaffDetail("2") } returns secondaryPomDetail
    every { staffRepository.findByStaffId("1".toBigInteger()) } returns StaffEntityFactory()
      .withStaffId(primaryPomDetail.staffId)
      .withFirstName(primaryPomDetail.firstName)
      .withLastName(primaryPomDetail.lastName)
      .withPrimaryEmail(primaryPomDetail.primaryEmail)
      .withUsername(primaryPomDetail.generalAccount?.username!!)
      .withAccountType(AccountType.GENERAL)
      .produce()

    every { staffRepository.findByStaffId("2".toBigInteger()) } returns StaffEntityFactory()
      .withStaffId(secondaryPomDetail.staffId)
      .withFirstName(secondaryPomDetail.firstName)
      .withLastName(secondaryPomDetail.lastName)
      .withPrimaryEmail(secondaryPomDetail.primaryEmail)
      .withUsername(secondaryPomDetail.generalAccount?.username!!)
      .withAccountType(AccountType.GENERAL)
      .produce()

    val (primaryPom, secondaryPom) = service.getOffenderAllocation(prisonNumber)

    assertEquals(primaryPomDetail.staffId, primaryPom?.staffId)
    assertEquals(secondaryPomDetail.staffId, secondaryPom?.staffId)

    verify {
      allocationManagerService.getOffenderAllocation(prisonNumber)
    }
  }

  @Test
  fun `getOffenderAllocation should throw exception if offender allocation is null`() {
    val prisonNumber = "A1234BC"
    every { allocationManagerService.getOffenderAllocation(prisonNumber) } returns null

    val exception = assertThrows<BusinessException> {
      service.getOffenderAllocation(prisonNumber)
    }

    assertEquals("No POM details found for A1234BC", exception.message)

    verify { allocationManagerService.getOffenderAllocation(prisonNumber) }
  }

  @Test
  fun `buildStaffEntity should build correct StaffEntity`() {
    val staffDetailResponse = StaffDetailResponse(
      staffId = "1".toBigInteger(),
      firstName = "John",
      lastName = "Doe",
      status = "ACTIVE",
      primaryEmail = null,
      generalAccount = Account("jdoe"),
      adminAccount = null,
    )

    val result = service.buildStaffEntity(staffDetailResponse)

    assertEquals("1".toBigInteger(), result.staffId)
    assertEquals("John", result.firstName)
    assertEquals("Doe", result.lastName)
    assertNull(result.primaryEmail)
    assertEquals("jdoe", result.username)
    assertEquals(AccountType.GENERAL.name, result.accountType)
  }

  @Test
  fun `should log warning and return null when staffId is null`() {
    val prisonNumber = "G8335GI"
    val pomType = PomType.PRIMARY

    val result = service.fetchPomDetailsIfNotAlreadyExists(null, prisonNumber, pomType)

    assertNull(result)
    verify(exactly = 0) { staffRepository.findByStaffId(any()) }
  }

  @Test
  fun `should return existing staff entity if found in repository`() {
    // Arrange
    val staffId = "123".toBigInteger()
    val prisonNumber = "G8335GI"
    val pomType = PomType.PRIMARY
    val existingStaffEntity = mockk<StaffEntity>()

    every { staffRepository.findByStaffId(staffId) } returns existingStaffEntity

    // Act
    val result = service.fetchPomDetailsIfNotAlreadyExists(staffId, prisonNumber, pomType)

    // Assert
    assertEquals(existingStaffEntity, result)
    verify { staffRepository.findByStaffId(staffId) }
    verify(exactly = 0) { nomisUserRolesService.getStaffDetail(any()) }
    verify(exactly = 0) { staffRepository.save(any()) }
  }
}
