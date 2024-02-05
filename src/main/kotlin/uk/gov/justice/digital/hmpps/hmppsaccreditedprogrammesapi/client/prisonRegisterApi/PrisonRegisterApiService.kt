package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonDetails

@Service
@Transactional
class PrisonRegisterApiService
@Autowired
constructor(
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
  fun getAllPrisons(): Map<String?, String?> {
    return getPrisonDetails { it.associate { details -> details.prisonId to details.prisonName } }
  }

  fun getPrisonById(prisonId: String): PrisonDetails? {
    return getPrisonDetails { it.firstOrNull() }
  }

  private inline fun <reified T> getPrisonDetails(
    retrieveResult: (List<PrisonDetails>) -> T,
  ): T {
    val prisonDetailsResult = when (val result = prisonRegisterApiClient.getAllPrisonDetails()) {
      is ClientResult.Success -> AuthorisableActionResult.Success(result.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${result.status} reason ${result.toException().message}")
        AuthorisableActionResult.Success(emptyList())
      }
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${result.toException().message}")
        AuthorisableActionResult.Success(emptyList())
      }
    }

    return retrieveResult(prisonDetailsResult.entity)
  }
}
