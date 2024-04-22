
package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.junit.jupiter.api.Test
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage

class DomainEventsListenerTest : IntegrationTestBase() {
  @Test
  fun `update offender message`() {
    val eventType = "prisoner-offender-search.prisoner.updated"
    val nomsNumber = "N321321"
    sendDomainEvent(
      DomainEventsMessage(
        eventType,
        additionalInformation = mapOf("nomsNumber" to nomsNumber),
      ),
    )
    verify(referralService, timeout(20000)).updatePerson(nomsNumber)
  }
}
