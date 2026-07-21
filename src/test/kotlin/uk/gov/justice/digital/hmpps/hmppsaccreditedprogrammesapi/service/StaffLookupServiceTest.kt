package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffIdSurnameProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.UsernameSurnameProjection
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

  @Test
  fun `resolveSurnamesByUsername returns a map keyed by username for every match`() {
    every {
      staffRepository.findSurnamesByUsernames(setOf("ARIVER", "BSMITH"))
    } returns listOf(
      usernameProjection("ARIVER", "River"),
      usernameProjection("BSMITH", "Smith"),
    )

    val result = service.resolveSurnamesByUsername(listOf("ARIVER", "BSMITH"))

    assertThat(result).containsExactlyInAnyOrderEntriesOf(
      mapOf("ARIVER" to "River", "BSMITH" to "Smith"),
    )
  }

  @Test
  fun `resolveSurnamesByUsername omits usernames with no matching staff row`() {
    every {
      staffRepository.findSurnamesByUsernames(setOf("ARIVER", "MISSING"))
    } returns listOf(usernameProjection("ARIVER", "River"))

    val result = service.resolveSurnamesByUsername(listOf("ARIVER", "MISSING"))

    assertThat(result).containsOnly(java.util.Map.entry("ARIVER", "River"))
  }

  @Test
  fun `resolveSurnamesByUsername picks the first surname when the username has duplicate rows`() {
    every {
      staffRepository.findSurnamesByUsernames(setOf("ARIVER"))
    } returns listOf(
      usernameProjection("ARIVER", "River"),
      usernameProjection("ARIVER", "Rivera"),
    )

    val result = service.resolveSurnamesByUsername(listOf("ARIVER"))

    assertThat(result).containsOnly(java.util.Map.entry("ARIVER", "River"))
  }

  @Test
  fun `resolveSurnamesByUsername drops nulls and blanks before hitting the repository`() {
    every { staffRepository.findSurnamesByUsernames(setOf("ARIVER")) } returns
      listOf(usernameProjection("ARIVER", "River"))

    val result = service.resolveSurnamesByUsername(listOf("ARIVER", null, "", "   "))

    assertThat(result).containsOnly(java.util.Map.entry("ARIVER", "River"))
    verify(exactly = 1) { staffRepository.findSurnamesByUsernames(setOf("ARIVER")) }
  }

  @Test
  fun `resolveSurnamesByUsername short-circuits when all inputs are null or blank`() {
    val result = service.resolveSurnamesByUsername(listOf(null, "", "  "))

    assertThat(result).isEmpty()
    verify(exactly = 0) { staffRepository.findSurnamesByUsernames(any()) }
  }

  @Test
  fun `resolveSurnamesByStaffId returns a map keyed by staff id for every match`() {
    val a = BigInteger.valueOf(1)
    val b = BigInteger.valueOf(2)
    every {
      staffRepository.findSurnamesByStaffIds(setOf(a, b))
    } returns listOf(
      staffIdProjection(a, "River"),
      staffIdProjection(b, "Smith"),
    )

    val result = service.resolveSurnamesByStaffId(listOf(a, b))

    assertThat(result).containsExactlyInAnyOrderEntriesOf(mapOf(a to "River", b to "Smith"))
  }

  @Test
  fun `resolveSurnamesByStaffId omits staff ids with no matching row`() {
    val a = BigInteger.valueOf(1)
    val b = BigInteger.valueOf(2)
    every {
      staffRepository.findSurnamesByStaffIds(setOf(a, b))
    } returns listOf(staffIdProjection(a, "River"))

    val result = service.resolveSurnamesByStaffId(listOf(a, b))

    assertThat(result).containsOnly(java.util.Map.entry(a, "River"))
  }

  @Test
  fun `resolveSurnamesByStaffId picks the first surname when the staff id has duplicate rows`() {
    val a = BigInteger.valueOf(1)
    every {
      staffRepository.findSurnamesByStaffIds(setOf(a))
    } returns listOf(
      staffIdProjection(a, "River"),
      staffIdProjection(a, "Rivera"),
    )

    val result = service.resolveSurnamesByStaffId(listOf(a))

    assertThat(result).containsOnly(java.util.Map.entry(a, "River"))
  }

  @Test
  fun `resolveSurnamesByStaffId drops nulls before hitting the repository`() {
    val a = BigInteger.valueOf(1)
    every { staffRepository.findSurnamesByStaffIds(setOf(a)) } returns
      listOf(staffIdProjection(a, "River"))

    val result = service.resolveSurnamesByStaffId(listOf(a, null))

    assertThat(result).containsOnly(java.util.Map.entry(a, "River"))
    verify(exactly = 1) { staffRepository.findSurnamesByStaffIds(setOf(a)) }
  }

  @Test
  fun `resolveSurnamesByStaffId short-circuits when all inputs are null`() {
    val result = service.resolveSurnamesByStaffId(listOf(null, null))

    assertThat(result).isEmpty()
    verify(exactly = 0) { staffRepository.findSurnamesByStaffIds(any()) }
  }

  private fun usernameProjection(usernameValue: String, lastNameValue: String): UsernameSurnameProjection = object : UsernameSurnameProjection {
    override val username: String = usernameValue
    override val lastName: String = lastNameValue
  }

  private fun staffIdProjection(staffIdValue: BigInteger, lastNameValue: String): StaffIdSurnameProjection = object : StaffIdSurnameProjection {
    override val staffId: BigInteger = staffIdValue
    override val lastName: String = lastNameValue
  }
}
