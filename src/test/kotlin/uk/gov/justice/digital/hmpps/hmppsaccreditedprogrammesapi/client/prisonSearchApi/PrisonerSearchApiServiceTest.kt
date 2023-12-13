package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_2
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_2

class PrisonerSearchApiServiceTest {

  private val prisonerSearchApiClient = mockk<PrisonerSearchApiClient>()
  val service = PrisonerSearchApiService(prisonerSearchApiClient)

  @Test
  fun `should return list of prisoners`() {
    val prisonerDetails = listOf(PRISONER_1, PRISONER_2)

    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Success(HttpStatus.OK, prisonerDetails)

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1, PRISON_NUMBER_2))

    val expectedMap = mapOf(PRISON_NUMBER_1 to listOf(PRISONER_1), PRISON_NUMBER_2 to listOf(PRISONER_2))

    assertEquals(expectedMap, result)
  }

  @Test
  fun `get prisoners call returns empty list when there is a failure fetching prisoners details`() {
    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/prisoner-search/prisoner-numbers",
      HttpStatusCode.valueOf(400),
      "",
    )

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1, PRISON_NUMBER_2))

    assertTrue(result.isEmpty())
  }
}
