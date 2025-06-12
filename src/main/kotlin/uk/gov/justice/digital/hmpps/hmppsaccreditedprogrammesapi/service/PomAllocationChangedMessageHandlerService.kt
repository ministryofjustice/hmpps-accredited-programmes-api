package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PomAllocationChangedMessageHandlerService(
  private val personService: PersonService,
  private val referralService: ReferralService,
  private val staffService: StaffService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun updatePrisonerPOMAllocation(prisonNumber: String) {
    log.info("START: Started processing PomAllocationChangedMessage for prisoner $prisonNumber")
    val person = personService.getPerson(prisonNumber)

    if (person == null) {
      log.info("Prisoner is not of interest to ACP: $prisonNumber")
      return
    }

    try {
      val (primaryPom, secondaryPom) = staffService.getOffenderAllocation(prisonNumber)
      referralService.updatePoms(prisonNumber, primaryPom, secondaryPom)
      log.info("FINISH: Processed PomAllocationChangedMessage for prisoner $prisonNumber")
    } catch (e: Exception) {
      log.error("Failed to process PomAllocationChangedMessage for prisoner $prisonNumber", e)
    }
  }
}
