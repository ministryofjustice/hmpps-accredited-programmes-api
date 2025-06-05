package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.PersonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.StaffEntity

class PomAllocationChangedMessageHandlerServiceTest {
  private val personService: PersonService = mockk()
  private val referralService: ReferralService = mockk(relaxed = true)
  private val staffService: StaffService = mockk()
  private lateinit var handler: PomAllocationChangedMessageHandlerService

  @BeforeEach
  fun setUp() {
    handler = PomAllocationChangedMessageHandlerService(personService, referralService, staffService)
  }

  @Test
  fun `should process message and update POMs when person exists`() {
    // Given
    val prisonNumber = "A1234BC"
    val person = mockk<PersonEntity>()
    val primaryPom = mockk<StaffEntity>()
    val secondaryPom = mockk<StaffEntity>()

    every { personService.getPerson(prisonNumber) } returns person
    every { staffService.getOffenderAllocation(prisonNumber) } returns Pair(primaryPom, secondaryPom)

    // When
    handler.process(prisonNumber)

    // Then
    verify { personService.getPerson(prisonNumber) }
    verify { staffService.getOffenderAllocation(prisonNumber) }
    verify { referralService.updatePoms(prisonNumber, primaryPom, secondaryPom) }
  }

  @Test
  fun `should return when person does not exist`() {
    // Given
    val prisonNumber = "Z9999XY"
    every { personService.getPerson(prisonNumber) } returns null

    // When
    handler.process(prisonNumber)

    // Then
    verify(exactly = 1) { personService.getPerson(prisonNumber) }
    verify(exactly = 0) { staffService.getOffenderAllocation(any()) }
    verify(exactly = 0) { referralService.updatePoms(any(), any(), any()) }
  }

  @Test
  fun `should catch exception when staffService or referralService throws exception`() {
    // Given
    val prisonNumber = "B5678DE"
    val person = mockk<PersonEntity>()
    val primaryPom = mockk<StaffEntity>()
    val secondaryPom = mockk<StaffEntity>()

    every { personService.getPerson(prisonNumber) } returns person
    every { staffService.getOffenderAllocation(prisonNumber) } returns Pair(primaryPom, secondaryPom)
    every { referralService.updatePoms(prisonNumber, primaryPom, secondaryPom) } throws RuntimeException("Service failure")

    // When
    handler.process(prisonNumber)

    // Then
    verify { personService.getPerson(prisonNumber) }
    verify { staffService.getOffenderAllocation(prisonNumber) }
    verify { referralService.updatePoms(prisonNumber, primaryPom, secondaryPom) }
  }
}
