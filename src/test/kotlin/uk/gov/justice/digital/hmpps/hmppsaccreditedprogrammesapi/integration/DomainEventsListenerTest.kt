package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.Identifiers
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.PersonReference

class DomainEventsListenerTest : IntegrationTestBase() {
    @Test
    fun `update offender message`() {
        val eventType = "prisoner-offender-search.prisoner.updated"
        val crn = "B123123B"
        val nomsNumber = "N321321"
        sendDomainEvent(
            DomainEventsMessage(
                eventType,
                PersonReference(listOf(Identifiers("CRN", crn))),
               additionalInformation = mapOf("nomsNumber" to nomsNumber),
            )
        )
        verify(referralService, timeout(5000)).updatePerson(nomsNumber)
    }
}
