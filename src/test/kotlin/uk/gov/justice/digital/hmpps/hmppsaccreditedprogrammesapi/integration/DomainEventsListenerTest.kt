package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener.DomainEventsMessage
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
@Isolated
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
    await untilCallTo {
      domainEventQueueClient.countMessagesOnQueue(domainEventQueue.queueUrl).get()
    } matches { it == 0 }
    verify(referralService, timeout(20000)).updatePerson(nomsNumber)
  }
}
