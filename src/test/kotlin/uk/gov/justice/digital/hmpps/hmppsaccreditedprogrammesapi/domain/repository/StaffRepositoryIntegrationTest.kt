package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.math.BigInteger
import java.util.UUID
import kotlin.test.Test

/**
 * Integration coverage for the scalar surname-projection queries on
 * [StaffRepository]. Guards the hand-rolled JPQL strings against silent
 * breakage from a future rename of `StaffEntity.username` / `staffId` /
 * `lastName`.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StaffRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var staffRepository: StaffRepository

  private val seededStaffId: BigInteger = "12345".toBigInteger()

  private fun seedStaffRow() {
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      staffId = seededStaffId,
      firstName = "Alex",
      lastName = "River",
      username = "ARIVER",
      primaryEmail = "alex.river@justice.gov.uk",
    )
  }

  @Test
  fun `findLastNameByUsername returns the surname for a seeded username`() {
    // Given
    seedStaffRow()

    // When
    val result = staffRepository.findLastNameByUsername("ARIVER")

    // Then
    result.shouldContainExactly("River")
  }

  @Test
  fun `findLastNameByUsername returns empty list when no staff row matches`() {
    // Given
    persistenceHelper.clearAllTableContent()

    // When
    val result = staffRepository.findLastNameByUsername("DOES_NOT_EXIST")

    // Then
    result.shouldBeEmpty()
  }

  @Test
  fun `findLastNameByStaffId returns the surname for a seeded staffId`() {
    // Given
    seedStaffRow()

    // When
    val result = staffRepository.findLastNameByStaffId(seededStaffId)

    // Then
    result.shouldContainExactly("River")
  }

  @Test
  fun `findLastNameByStaffId returns empty list when no staff row matches`() {
    // Given
    persistenceHelper.clearAllTableContent()

    // When
    val result = staffRepository.findLastNameByStaffId(BigInteger.valueOf(9_999_999))

    // Then
    result.shouldBeEmpty()
  }

  @Test
  fun `findSurnamesByUsernames returns a projection row per matched staff username`() {
    // Given
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      staffId = "1".toBigInteger(),
      firstName = "Alex",
      lastName = "River",
      username = "ARIVER",
      primaryEmail = "a@justice.gov.uk",
    )
    persistenceHelper.createStaff(
      staffId = "2".toBigInteger(),
      firstName = "Bea",
      lastName = "Smith",
      username = "BSMITH",
      primaryEmail = "b@justice.gov.uk",
    )

    // When
    val result = staffRepository.findSurnamesByUsernames(listOf("ARIVER", "BSMITH", "MISSING"))

    // Then – MISSING is silently dropped by the IN clause
    val byUsername = result.associate { it.username to it.lastName }
    byUsername shouldBe mapOf("ARIVER" to "River", "BSMITH" to "Smith")
  }

  @Test
  fun `findSurnamesByUsernames returns empty list when no usernames match`() {
    // Given
    persistenceHelper.clearAllTableContent()

    // When
    val result = staffRepository.findSurnamesByUsernames(listOf("DOES_NOT_EXIST"))

    // Then
    result.shouldBeEmpty()
  }

  @Test
  fun `findSurnamesByStaffIds returns a projection row per matched staff id`() {
    // Given
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      staffId = "1".toBigInteger(),
      firstName = "Alex",
      lastName = "River",
      username = "ARIVER",
      primaryEmail = "a@justice.gov.uk",
    )
    persistenceHelper.createStaff(
      staffId = "2".toBigInteger(),
      firstName = "Bea",
      lastName = "Smith",
      username = "BSMITH",
      primaryEmail = "b@justice.gov.uk",
    )

    // When
    val result = staffRepository.findSurnamesByStaffIds(
      listOf("1".toBigInteger(), "2".toBigInteger(), "9999".toBigInteger()),
    )

    // Then – unmatched IDs are silently dropped by the IN clause
    val byStaffId = result.associate { it.staffId to it.lastName }
    byStaffId shouldBe mapOf(
      "1".toBigInteger() to "River",
      "2".toBigInteger() to "Smith",
    )
  }

  @Test
  fun `findSurnamesByStaffIds returns empty list when no staff ids match`() {
    // Given
    persistenceHelper.clearAllTableContent()

    // When
    val result = staffRepository.findSurnamesByStaffIds(listOf("9999".toBigInteger()))

    // Then
    result.shouldBeEmpty()
  }

  // -------------------------------------------------------------------------
  // Deterministic-ordering guarantees for duplicate username / staffId rows.
  // The projection queries add `ORDER BY <key>, s.id` so that callers grouping
  // by the key and taking `.first()` always pick the row with the smallest UUID.
  // These tests seed two rows with the same lookup key but different UUIDs so
  // the ordering has something to actually order.
  // -------------------------------------------------------------------------

  private val idAlpha: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
  private val idBravo: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")

  @Test
  fun `findLastNameByUsername returns rows ordered by id when the username has duplicates`() {
    // Given – two rows share username DUP, distinct UUIDs; alpha's UUID sorts first.
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      id = idBravo,
      staffId = "20".toBigInteger(),
      firstName = "Bea",
      lastName = "Bravo",
      username = "DUP",
      primaryEmail = "b@justice.gov.uk",
    )
    persistenceHelper.createStaff(
      id = idAlpha,
      staffId = "10".toBigInteger(),
      firstName = "Alex",
      lastName = "Alpha",
      username = "DUP",
      primaryEmail = "a@justice.gov.uk",
    )

    // When
    val result = staffRepository.findLastNameByUsername("DUP")

    // Then – Alpha wins because its UUID sorts first.
    result.shouldContainExactly("Alpha", "Bravo")
  }

  @Test
  fun `findSurnamesByUsernames orders duplicate rows by id so first() is stable`() {
    // Given
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      id = idBravo,
      staffId = "20".toBigInteger(),
      firstName = "Bea",
      lastName = "Bravo",
      username = "DUP",
      primaryEmail = "b@justice.gov.uk",
    )
    persistenceHelper.createStaff(
      id = idAlpha,
      staffId = "10".toBigInteger(),
      firstName = "Alex",
      lastName = "Alpha",
      username = "DUP",
      primaryEmail = "a@justice.gov.uk",
    )

    // When
    val result = staffRepository.findSurnamesByUsernames(listOf("DUP"))

    // Then – both rows returned in a stable order (alpha's UUID first).
    result.map { it.lastName } shouldBe listOf("Alpha", "Bravo")
  }

  @Test
  fun `findSurnamesByStaffIds orders duplicate rows by id so first() is stable`() {
    // Given – two rows share staffId 42, distinct UUIDs.
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createStaff(
      id = idBravo,
      staffId = "42".toBigInteger(),
      firstName = "Bea",
      lastName = "Bravo",
      username = "BB",
      primaryEmail = "b@justice.gov.uk",
    )
    persistenceHelper.createStaff(
      id = idAlpha,
      staffId = "42".toBigInteger(),
      firstName = "Alex",
      lastName = "Alpha",
      username = "AA",
      primaryEmail = "a@justice.gov.uk",
    )

    // When
    val result = staffRepository.findSurnamesByStaffIds(listOf("42".toBigInteger()))

    // Then
    result.map { it.lastName } shouldBe listOf("Alpha", "Bravo")
  }
}
