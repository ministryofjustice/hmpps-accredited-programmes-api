package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_ID_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NAME_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService

class PrisonRegisterApiServiceTest {

  private val prisonRegisterApiClient = mockk<PrisonRegisterApiClient>()
  val service = PrisonRegisterApiService(prisonRegisterApiClient)

  @Test
  fun `should return a prison`() {
    val prisonDetails = Prison(
      prisonId = PRISON_ID_1,
      prisonName = PRISON_NAME_1,
      active = false,
      male = false,
      female = true,
      contracted = true,
      types = emptyList(),
      categories = emptySet(),
      addresses = emptyList(),
      operators = emptyList(),
    )

    every { prisonRegisterApiClient.getPrison(PRISON_ID_1) } returns ClientResult.Success(HttpStatus.OK, prisonDetails)

    val result = service.getPrisonById(PRISON_ID_1)

    assertEquals(prisonDetails, result)
  }
}
