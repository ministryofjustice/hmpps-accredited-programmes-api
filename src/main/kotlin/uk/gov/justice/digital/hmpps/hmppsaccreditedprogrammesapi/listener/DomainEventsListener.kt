package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService

@Service
class DomainEventsListener(
  private val referralService: ReferralService,
  private val objectMapper: ObjectMapper,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun listen(msg: String) {
    val (message, attributes) = objectMapper.readValue<SQSMessage>(msg)
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(message)
    if (attributes.eventType == "prisoner-offender-search.prisoner.updated") {
      log.debug("Processing prisoner offender search update message")
      handleMessage(domainEventMessage)
    }
  }

  private fun handleMessage(message: DomainEventsMessage) {
    if (message.prisonerNumber != null) {
      referralService.updatePerson(message.prisonerNumber)
    } else {
      log.error("Message did not contain prisoner number. " + message.additionalInformation)
    }
  }
}

data class DomainEventsMessage(
  val eventType: String,
  val personReference: PersonReference,
  val additionalInformation: Map<String, Any>? = mapOf(),
) {
  val prisonerNumber = additionalInformation?.get("nomsNumber") as String?
}

data class PersonReference(
  val identifiers: List<Identifiers>,
)

data class Identifiers(
  val type: String,
  val value: String,
)

data class SQSMessage(
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
)

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {

  val eventType = attributes[EVENT_TYPE_KEY]?.value

  companion object {
    private const val EVENT_TYPE_KEY = "eventType"
  }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
