package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.listener

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PomAllocationChangedMessageHandlerService

@Service
class DomainEventsListener(
  private val personService: PersonService,
  private val objectMapper: ObjectMapper,
  private val pomAllocationChangedMessageHandlerService: PomAllocationChangedMessageHandlerService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  @SqsListener("hmppsdomaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun listen(msg: String) {
    val (message) = objectMapper.readValue<SQSMessage>(msg)
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(message)
    val prisonNumber = domainEventMessage.prisonerNumber ?: domainEventMessage.personReference.findNomsNumber()
    log.info("Request received to process domain event type ${domainEventMessage.eventType} for prisoner number $prisonNumber")
    handleMessage(domainEventMessage)
  }

  private fun handleMessage(message: DomainEventsMessage) {
    when (message.eventType) {
      "prisoner-offender-search.prisoner.updated" -> handlePrisonerUpdatedMessage(message)
      "offender-management.allocation.changed" -> handlePomAllocationChangedMessage(message)
      "probation-case.requirement.created" -> log.info("Ignoring probation-case.requirement.created event \n $message")
      else -> log.error("Unknown event type: ${message.eventType}")
    }
  }

  private fun handlePomAllocationChangedMessage(message: DomainEventsMessage) {
    message.personReference.findNomsNumber()?.let {
      pomAllocationChangedMessageHandlerService.updatePrisonerPOMAllocation(it)
    } ?: log.error("Pom allocation message did not contain prisoner number. " + message.additionalInformation)
  }

  private fun handlePrisonerUpdatedMessage(message: DomainEventsMessage) {
    message.prisonerNumber?.let {
      personService.updatePerson(it)
    } ?: log.error("Prison offender message did not contain prisoner number. " + message.additionalInformation)
  }
}

data class DomainEventsMessage(
  val eventType: String,
  val additionalInformation: Map<String, Any>? = mapOf(),
  val personReference: PersonReference = PersonReference(),
) {
  val prisonerNumber = additionalInformation?.get("nomsNumber") as String?
}

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsNumber() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}
data class PersonIdentifier(val type: String, val value: String)

data class SQSMessage(
  @JsonProperty("Message") val message: String,
)
