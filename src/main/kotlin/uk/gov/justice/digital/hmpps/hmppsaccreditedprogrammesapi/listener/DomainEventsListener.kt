package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService

@Service
class DomainEventsListener(
  private val personService: PersonService,
  private val objectMapper: ObjectMapper,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun listen(msg: String) {
    val (message) = objectMapper.readValue<SQSMessage>(msg)
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(message)
    log.info("Processing prisoner offender search update message")
    handleMessage(domainEventMessage)
  }

  private fun handleMessage(message: DomainEventsMessage) {
    if (message.prisonerNumber != null) {
      log.info("message contained prisoner number: ${message.prisonerNumber}")
      personService.updatePerson(message.prisonerNumber)
    } else {
      log.error("Message did not contain prisoner number. " + message.additionalInformation)
    }
  }
}

data class DomainEventsMessage(
  val eventType: String,
  val additionalInformation: Map<String, Any>? = mapOf(),
) {
  val prisonerNumber = additionalInformation?.get("nomsNumber") as String?
}

data class SQSMessage(
  @JsonProperty("Message") val message: String,
)
