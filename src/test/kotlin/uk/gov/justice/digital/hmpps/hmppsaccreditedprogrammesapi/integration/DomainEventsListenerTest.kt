package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.any
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

@Isolated
class DomainEventsListenerTest : IntegrationTestBase() {

  @SpyBean
  lateinit var referralService: ReferralService

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
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }
    verify(referralService, timeout(2000)).updatePerson(any())
  }
}
