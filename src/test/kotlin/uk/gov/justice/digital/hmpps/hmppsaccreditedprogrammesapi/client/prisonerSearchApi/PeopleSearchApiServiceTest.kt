package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.PeopleSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PeopleSearchRequest
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService

class PeopleSearchApiServiceTest {

  private val prisonerSearchApiClient = mockk<PrisonerSearchApiClient>()
  val service = PeopleSearchApiService(prisonerSearchApiClient)

  @Test
  fun `should return list of prisoners`() {
    val prisonerDetails = listOf(PRISONER_1)

    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Success(HttpStatus.OK, prisonerDetails)

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1))
    assertEquals(listOf(PRISONER_1), result)
  }

  @Test
  fun `get prisoners call returns empty list when there is a failure fetching prisoners details`() {
    every { prisonerSearchApiClient.getPrisonersByPrisonNumbers(any()) } returns ClientResult.Failure.StatusCode(
      HttpMethod.GET,
      "/prisoner-search/prisoner-numbers",
      HttpStatusCode.valueOf(400),
      "",
    )

    val result = service.getPrisoners(listOf(PRISON_NUMBER_1))

    assertTrue(result.isEmpty())
  }

  @Test
  fun `searchPeople returns matches when match-prisoners finds the person (no fallback)`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1)
    val match = peopleSearchResponse(PRISON_NUMBER_1)

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, listOf(match))

    val result = service.searchPeople(request)

    assertEquals(listOf(match), result)
    verify(exactly = 0) { prisonerSearchApiClient.getPeopleByPrisonNumbers(any()) }
  }

  @Test
  fun `searchPeople falls back to the exact prisoner-numbers lookup when match-prisoners finds nothing`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1)
    val match = peopleSearchResponse(PRISON_NUMBER_1)

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, emptyList())
    every { prisonerSearchApiClient.getPeopleByPrisonNumbers(listOf(PRISON_NUMBER_1)) } returns ClientResult.Success(HttpStatus.OK, listOf(match))

    val result = service.searchPeople(request)

    assertEquals(listOf(match), result)
  }

  @Test
  fun `searchPeople fallback respects the prisonIds filter`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1, prisonIds = listOf("MDI"))
    val inMdi = peopleSearchResponse(PRISON_NUMBER_1, prisonId = "MDI")
    val inLei = peopleSearchResponse(PRISON_NUMBER_1, prisonId = "LEI")

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, emptyList())
    every { prisonerSearchApiClient.getPeopleByPrisonNumbers(listOf(PRISON_NUMBER_1)) } returns ClientResult.Success(HttpStatus.OK, listOf(inLei, inMdi))

    val result = service.searchPeople(request)

    assertEquals(listOf(inMdi), result)
  }

  @Test
  fun `searchPeople returns empty list when both match-prisoners and prisoner-numbers find nothing`() {
    val request = PeopleSearchRequest(prisonerIdentifier = PRISON_NUMBER_1)

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, emptyList())
    every { prisonerSearchApiClient.getPeopleByPrisonNumbers(listOf(PRISON_NUMBER_1)) } returns ClientResult.Success(HttpStatus.OK, emptyList())

    val result = service.searchPeople(request)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `searchPeople does not fall back for a name-only search`() {
    val request = PeopleSearchRequest(firstName = "MICKEY", lastName = "SMITH")

    every { prisonerSearchApiClient.peopleSearch(request) } returns ClientResult.Success(HttpStatus.OK, emptyList())

    val result = service.searchPeople(request)

    assertTrue(result.isEmpty())
    verify(exactly = 0) { prisonerSearchApiClient.getPeopleByPrisonNumbers(any()) }
  }

  private fun peopleSearchResponse(prisonerNumber: String, prisonId: String? = null) = PeopleSearchResponse(
    bookingId = null,
    conditionalReleaseDate = null,
    prisonId = prisonId,
    prisonName = null,
    dateOfBirth = null,
    ethnicity = null,
    gender = null,
    homeDetentionCurfewEligibilityDate = null,
    indeterminateSentence = null,
    firstName = null,
    lastName = null,
    paroleEligibilityDate = null,
    prisonerNumber = prisonerNumber,
    religion = null,
    sentenceExpiryDate = null,
    sentenceStartDate = null,
    tariffDate = null,
  )
}
