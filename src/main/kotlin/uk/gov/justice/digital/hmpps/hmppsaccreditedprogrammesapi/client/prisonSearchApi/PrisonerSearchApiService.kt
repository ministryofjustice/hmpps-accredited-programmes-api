package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.approvedpremisesapi.results.AuthorisableActionResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.problems.ForbiddenProblem

@Service
@Transactional
class PrisonerSearchApiService
@Autowired
constructor(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {

  fun getPrisoners(prisonerNumbers: List<String>): Map<String?, List<Prisoner>> {
    val prisonerDetails = when (val allPrisonDetails = prisonerSearchApiClient.getPrisonersByPrisonNumbers(prisonerNumbers)) {
      is ClientResult.Success -> AuthorisableActionResult.Success(allPrisonDetails.body)
      is ClientResult.Failure.StatusCode -> if (allPrisonDetails.status == HttpStatus.NOT_FOUND) AuthorisableActionResult.NotFound() else allPrisonDetails.throwException()
      is ClientResult.Failure -> allPrisonDetails.throwException()
    }

    val prisoners = when (prisonerDetails) {
      is AuthorisableActionResult.NotFound -> throw Exception("No prison details found")
      is AuthorisableActionResult.Unauthorised -> throw ForbiddenProblem()
      is AuthorisableActionResult.Success -> prisonerDetails.entity
    }

    return prisoners.groupBy { it.prisonerNumber }
  }
}
