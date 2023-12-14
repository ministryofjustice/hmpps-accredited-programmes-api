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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonSearchApi.model.Prisoner

private const val PRISON_NUMBER_1 = "1"

private const val PRISON_NUMBER_2 = "2"

class PrisonerSearchApiServiceTest {

  private val prisonerSearchApiClient = mockk<PrisonerSearchApiClient>()
  val service = PrisonerSearchApiService(prisonerSearchApiClient)

  val prisoner1 = Prisoner(prisonerNumber = PRISON_NUMBER_1, firstName = "John", lastName = "Doe")
  val prisoner2 = Prisoner(prisonerNumber = PRISON_NUMBER_2, firstName = "Ella", lastName = "Smith")

  @Test
  fun `should return list of prisoners`() {
    val prisonerDetails = listOf(prisoner1, prisoner2)

    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Success(HttpStatus.OK, prisonerDetails)

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1, PRISON_NUMBER_2))

    val expectedMap = mapOf(PRISON_NUMBER_1 to listOf(prisoner1), PRISON_NUMBER_2 to listOf(prisoner2))

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
