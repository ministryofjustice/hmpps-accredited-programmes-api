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

  fun getPrisonById(prisonId: String): PrisonDetails? {
    val prisonDetailsResult = when (val prison = prisonRegisterApiClient.getPrisonDetailsByPrisonId(prisonId)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(prison.body)
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to retrieve data. Status code ${prison.status} reason ${prison.toException().message}")
        AuthorisableActionResult.Success(null)
      }
      is ClientResult.Failure -> {
        log.warn("Failure to retrieve data ${prison.toException().message}")
        AuthorisableActionResult.Success(null)
      }
    }

    return prisonDetailsResult.entity
  }
}
