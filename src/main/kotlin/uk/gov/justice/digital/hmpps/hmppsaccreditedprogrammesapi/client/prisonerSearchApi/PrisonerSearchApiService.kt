package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner

@Service
@Transactional
class PrisonerSearchApiService
@Autowired
constructor(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisoners(prisonerNumbers: List<String>): List<Prisoner> {
    val prisoners = when (val response = prisonerSearchApiClient.getPrisonersByPrisonNumbers(prisonerNumbers)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(response.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${response.status}")
        AuthorisableActionResult.Success(listOf<Prisoner>())
      }

      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data")
        AuthorisableActionResult.Success(listOf<Prisoner>())
      }
    }

    return prisoners.entity
  }
}
