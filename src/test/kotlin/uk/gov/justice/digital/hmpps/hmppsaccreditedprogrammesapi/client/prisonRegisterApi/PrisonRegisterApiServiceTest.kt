package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_ID_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_ID_2
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME_2

class PrisonRegisterApiServiceTest {

  private val prisonRegisterApiClient = mockk<PrisonRegisterApiClient>()
  val service = PrisonRegisterApiService(prisonRegisterApiClient)

  @Test
  fun `should return list of prisons`() {
    val prisonDetails = listOf(PrisonDetails(PRISON_ID_1, PRISON_NAME_1), PrisonDetails(PRISON_ID_2, PRISON_NAME_2))

    every { prisonRegisterApiClient.getAllPrisonDetails() } returns ClientResult.Success(HttpStatus.OK, prisonDetails)

    val result = service.getAllPrisons()

    val expectedMap = mapOf(PRISON_ID_1 to PRISON_NAME_1, PRISON_ID_2 to PRISON_NAME_2)

    assertEquals(expectedMap, result)
  }

  @Test
  fun `getAllPrisons call returns empty list when there is a failure fetching prison details`() {
    every { prisonRegisterApiClient.getAllPrisonDetails() } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/prisons/names",
      HttpStatusCode.valueOf(400),
      "",
    )

    val resultGeneralFailure = service.getAllPrisons()

    assertTrue(resultGeneralFailure.isEmpty())
  }
}
