package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import java.math.BigInteger

class StaffLookupServiceTest {

  private val staffRepository: StaffRepository = mockk()
  private val service = StaffLookupService(staffRepository)

  @Test
  fun `findLastNameByUsername returns surname when a matching staff row exists`() {
    every { staffRepository.findLastNameByUsername("ARIVER") } returns listOf("River")

    assertThat(service.findLastNameByUsername("ARIVER")).isEqualTo("River")

    verify(exactly = 1) { staffRepository.findLastNameByUsername("ARIVER") }
  }

  @Test
  fun `findLastNameByUsername returns null when no staff row matches`() {
    every { staffRepository.findLastNameByUsername("MISSING") } returns emptyList()

    assertThat(service.findLastNameByUsername("MISSING")).isNull()
  }

  @Test
  fun `findLastNameByUsername short-circuits on null input without hitting the repository`() {
    assertThat(service.findLastNameByUsername(null)).isNull()

    verify(exactly = 0) { staffRepository.findLastNameByUsername(any()) }
  }

  @Test
  fun `findLastNameByUsername short-circuits on blank input without hitting the repository`() {
    assertThat(service.findLastNameByUsername("   ")).isNull()

    verify(exactly = 0) { staffRepository.findLastNameByUsername(any()) }
  }

  @Test
  fun `findLastNameByStaffId returns surname when a matching staff row exists`() {
    val staffId = BigInteger.valueOf(12345)
    every { staffRepository.findLastNameByStaffId(staffId) } returns listOf("River")

    assertThat(service.findLastNameByStaffId(staffId)).isEqualTo("River")

    verify(exactly = 1) { staffRepository.findLastNameByStaffId(staffId) }
  }

  @Test
  fun `findLastNameByStaffId returns null when no staff row matches`() {
    val staffId = BigInteger.valueOf(99999)
    every { staffRepository.findLastNameByStaffId(staffId) } returns emptyList()

    assertThat(service.findLastNameByStaffId(staffId)).isNull()
  }

  @Test
  fun `findLastNameByStaffId short-circuits on null input without hitting the repository`() {
    assertThat(service.findLastNameByStaffId(null)).isNull()

    verify(exactly = 0) { staffRepository.findLastNameByStaffId(any()) }
  }
}
