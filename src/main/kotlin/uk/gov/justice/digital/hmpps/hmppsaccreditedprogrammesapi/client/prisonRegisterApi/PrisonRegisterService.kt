package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.problems.ForbiddenProblem

@Service
@Transactional
class PrisonRegisterService
@Autowired
constructor(
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
) {
  fun getAllPrisons(): Map<String?, String?> {
    val prisonDetailsResult = when (val allPrisonDetails = prisonRegisterApiClient.getAllPrisonDetails()) {
      is ClientResult.Success -> AuthorisableActionResult.Success(allPrisonDetails.body)
      is ClientResult.Failure.StatusCode -> if (allPrisonDetails.status == HttpStatus.NOT_FOUND) AuthorisableActionResult.NotFound() else allPrisonDetails.throwException()
      is ClientResult.Failure -> allPrisonDetails.throwException()
    }

    val prisonDetails = when (prisonDetailsResult) {
      is AuthorisableActionResult.NotFound -> throw Exception("No prison details found")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> prisonDetailsResult.entity
    }

    return prisonDetails.associate { it.prisonId to it.prisonName }
  }
}
